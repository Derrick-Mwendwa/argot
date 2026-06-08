plugins {
    alias(libs.plugins.kotlin.jvm)
    // mavenPublish is applied in Milestone 6.
}

kotlin {
    jvmToolchain(17)
    explicitApi()
}

// argot-annotations holds only the annotation declarations consumed by the KSP processor.
// Constraint: zero third-party runtime dependencies — stdlib only.
dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
