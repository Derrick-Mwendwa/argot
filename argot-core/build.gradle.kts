plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvmToolchain(17)

    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
        // Required on Kotlin 2.3.x. Kotlin 2.4 removes `enabled` and enabling is implicit, so this
        // becomes a bare abiValidation() then. Dropping it here silently disables the ABI check.
        enabled.set(true)
    }
    explicitApi()
}

mavenPublishing {
    pom {
        name.set("Argot Core")
        description.set("Zero-dependency Kotlin argument-parsing engine and delegate-style API.")
    }
}

dependencies {
    // No third-party runtime dependencies — stdlib only.
    testImplementation(platform(libs.junit.bom))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
