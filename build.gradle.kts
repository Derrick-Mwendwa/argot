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
}

dokka {
    moduleName.set("Argot")
    pluginsConfiguration.html {
        homepageLink.set("https://argot.draftcode.org")
        footerMessage.set("Argot — Apache 2.0")
        customStyleSheets.from(rootProject.file("site/dokka/argot.css"))
        // Overwrites images/logo-icon.svg, replacing Dokka's bundled Kotlin mark.
        customAssets.from(rootProject.file("site/dokka/logo-icon.svg"))
    }
}

// The root configuration above only styles the aggregate index. Each module generates its own pages,
// so the stylesheet has to be registered there too or the reference ships half-themed.
subprojects {
    plugins.withId("org.jetbrains.dokka") {
        extensions.configure<org.jetbrains.dokka.gradle.DokkaExtension> {
            pluginsConfiguration.html {
                homepageLink.set("https://argot.draftcode.org")
                footerMessage.set("Argot — Apache 2.0")
                customStyleSheets.from(rootProject.file("site/dokka/argot.css"))
                customAssets.from(rootProject.file("site/dokka/logo-icon.svg"))
            }
        }
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
