plugins {
    alias(libs.plugins.kotlin.jvm)
    // mavenPublish is applied in Milestone 6.
}

kotlin {
    jvmToolchain(17)
    explicitApi()
}

// argot-core is the pure-Kotlin parsing engine + delegate API.
// Constraint: zero third-party runtime dependencies — stdlib only.
dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
