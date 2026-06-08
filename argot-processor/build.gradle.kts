plugins {
    alias(libs.plugins.kotlin.jvm)
    // mavenPublish is applied in Milestone 6.
}

kotlin {
    jvmToolchain(17)
    // NB: explicitApi() is intentionally NOT enabled here. The processor's only public
    // symbol is its SymbolProcessorProvider (loaded by KSP via ServiceLoader); everything
    // else is `internal`. Strict explicit-API would add noise without consumer benefit, and
    // none of this code is ever on a consumer's runtime classpath. See README §toolchain.
}

// argot-processor is COMPILE-TIME ONLY. KSP + KotlinPoet must never reach a consumer runtime
// classpath — they are confined to this module, which a consumer applies via the KSP plugin.
dependencies {
    implementation(libs.ksp.symbolProcessingApi)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    // Referenced for annotation fully-qualified names only.
    implementation(project(":argot-annotations"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kctfork.ksp)
    // Test-only: the generated code references argot-core, so it must be on the compile-testing
    // classpath (inheritClassPath). argot-core is NOT a main dependency of the processor.
    testImplementation(project(":argot-core"))
}

tasks.test {
    useJUnitPlatform()
}
