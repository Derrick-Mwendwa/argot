import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.dokka)
}

// The API reference covers only what a consumer writes against. argot-processor is deliberately
// excluded: its one public symbol is the KSP entry point, which nobody calls by hand.
dependencies {
    dokka(project(":argot-core"))
    dokka(project(":argot-annotations"))

    // Swaps Dokka's HTML renderer for one that emits api.json. The site owns presentation from
    // there, so nothing here configures a theme.
    dokkaPlugin(project(":tools:dokka-json"))
}

dokka {
    moduleName.set("Argot")
}

// Each module runs its own generation, so the renderer has to be registered there too or those
// modules still emit HTML.
subprojects {
    plugins.withId("org.jetbrains.dokka") {
        dependencies { add("dokkaPlugin", project(":tools:dokka-json")) }
    }
}

allprojects {
    group = "org.draftcode"
    version = "0.1.2"
}

// The release workflow checks this against the git tag before publishing.
tasks.register("printVersion") {
    val projectVersion = version.toString()
    doLast { println(projectVersion) }
}

// Coordinates in prose are copy-paste targets, so a stale one hands users a version that may not
// have the API the surrounding text describes. Nothing else notices: the docs still build and the
// samples still compile, because they resolve the version from Gradle rather than from the page.
tasks.register("checkDocsVersions") {
    group = "verification"
    description = "Fails when documented Maven coordinates drift from the project version."

    val projectVersion = version.toString()
    val kspVersion = libs.versions.ksp.get()
    val kotlinVersion = libs.versions.kotlin.get()
    val files = listOf(
        rootProject.file("README.md"),
        rootProject.file("site/learn/tutorial/your-first-cli.md"),
    )
    inputs.files(files)
    inputs.property("version", projectVersion)
    inputs.property("ksp", kspVersion)
    inputs.property("kotlin", kotlinVersion)
    outputs.upToDateWhen { true }

    doLast {
        val expected = mapOf(
            Regex("""org\.draftcode:argot-[a-z]+:([\d.]+)""") to (projectVersion to "project version"),
            Regex("""id\("com\.google\.devtools\.ksp"\) version "([\d.]+)"""") to (kspVersion to "KSP"),
            Regex("""kotlin\("jvm"\) version "([\d.]+)"""") to (kotlinVersion to "Kotlin"),
        )

        val problems = files.filter { it.exists() }.flatMap { file ->
            file.readLines().withIndex().flatMap { (index, line) ->
                expected.mapNotNull { (pattern, want) ->
                    val found = pattern.find(line)?.groupValues?.get(1)
                    val (wanted, label) = want
                    if (found != null && found != wanted) {
                        "${file.name}:${index + 1} declares $label $found, expected $wanted"
                    } else {
                        null
                    }
                }
            }
        }

        if (problems.isNotEmpty()) {
            throw GradleException(
                problems.joinToString(
                    separator = "\n  ",
                    prefix = "Documented versions are stale:\n  ",
                    postfix = "\n\nUpdate the prose, or the catalog, so they agree.",
                )
            )
        }
    }
}

// `base` gives the root project the check/build lifecycle, so this runs as part of ./gradlew build
// rather than needing to be remembered.
apply(plugin = "base")

tasks.named("check") { dependsOn("checkDocsVersions") }

// Shared POM for any module applying the vanniktech plugin; each module sets its own name and
// description. Credentials come from Gradle properties or env vars (see RELEASING.md).
subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            signAllPublications() // skipped for -SNAPSHOT versions
            pom {
                inceptionYear.set("2026")
                url.set("https://github.com/Derrick-Mwendwa/argot")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("Derrick-Mwendwa")
                        name.set("Derrick Mwendwa")
                        url.set("https://github.com/Derrick-Mwendwa")
                    }
                }
                scm {
                    url.set("https://github.com/Derrick-Mwendwa/argot")
                    connection.set("scm:git:https://github.com/Derrick-Mwendwa/argot.git")
                    developerConnection.set("scm:git:ssh://git@github.com/Derrick-Mwendwa/argot.git")
                }
            }
        }
    }
}
