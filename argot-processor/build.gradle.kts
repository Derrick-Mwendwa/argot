plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish)
}

mavenPublishing {
    pom {
        name.set("Argot Processor")
        description.set("KSP2 processor that generates argument parsers from Argot annotations.")
    }
}

kotlin {
    jvmToolchain(17)

    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
        // Required on Kotlin 2.3.x. Kotlin 2.4 removes `enabled` and enabling is implicit, so this
        // becomes a bare abiValidation() then. Dropping it here silently disables the ABI check.
        enabled.set(true)
    }
    // explicitApi() intentionally off: the only public symbol is the SymbolProcessorProvider.
}

// Compile-time only: KSP + KotlinPoet must never reach a consumer runtime classpath.
dependencies {
    implementation(libs.ksp.symbolProcessingApi)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(project(":argot-annotations"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(kotlin("test"))
    testImplementation(libs.kctfork.ksp)
    testImplementation(project(":argot-core")) // test-only: generated code references argot-core
}

tasks.test {
    useJUnitPlatform()
}
