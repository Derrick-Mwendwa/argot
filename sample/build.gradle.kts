plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    application
}

kotlin {
    jvmToolchain(17)
}

// The sample demonstrates BOTH consumer styles and doubles as an end-to-end integration test.
// It is deliberately NOT published (no maven-publish plugin here).
dependencies {
    implementation(project(":argot-core"))
    implementation(project(":argot-annotations"))

    // Wires the annotation style through the real KSP2 processor, exercising it end-to-end.
    ksp(project(":argot-processor"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.draftcode.argot.sample.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
