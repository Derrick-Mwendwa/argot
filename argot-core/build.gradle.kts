plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvmToolchain(17)
    explicitApi()
}

mavenPublishing {
    pom {
        name.set("Argot Core")
        description.set("Zero-dependency Kotlin argument-parsing engine and delegate-style API.")
    }
}

// argot-core is the pure-Kotlin parsing engine + delegate API.
// Constraint: zero third-party runtime dependencies — stdlib only.
dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
