import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.mavenPublish) apply false
}

allprojects {
    group = "org.draftcode"
    version = "0.1.1"
}

// The release workflow checks this against the git tag before publishing.
tasks.register("printVersion") {
    val projectVersion = version.toString()
    doLast { println(projectVersion) }
}

// Shared POM for any module applying the vanniktech plugin; each module sets its own name and
// description. Credentials come from Gradle properties or env vars (see gradle.properties.template).
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
