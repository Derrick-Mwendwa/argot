import com.vanniktech.maven.publish.MavenPublishBaseExtension

// Root aggregator build. Plugins are declared here (without applying them) so that the
// version catalog stays the single source of versions; each module applies what it needs.
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.mavenPublish) apply false
}

allprojects {
    group = "org.draftcode"
    version = "0.1.0-SNAPSHOT"
}

// Shared publishing configuration for every module that applies the vanniktech plugin. Each module
// only sets its own POM name + description (and inherits coordinates org.draftcode:<module>:<version>
// from its project name automatically). No real credentials live here — see gradle.properties.template.
subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            // Publish to Maven Central via the Central Portal (the plugin's default target).
            publishToMavenCentral()
            // GPG sign all publications using an in-memory key from Gradle properties / env vars.
            // Signing is skipped automatically for -SNAPSHOT versions, so local builds need no key.
            signAllPublications()

            pom {
                inceptionYear.set("2026")
                url.set("https://github.com/draftcode/argot")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("draftcode")
                        name.set("Derek")
                        url.set("https://github.com/draftcode")
                    }
                }
                scm {
                    url.set("https://github.com/draftcode/argot")
                    connection.set("scm:git:https://github.com/draftcode/argot.git")
                    developerConnection.set("scm:git:ssh://git@github.com/draftcode/argot.git")
                }
            }
        }
    }
}
