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
