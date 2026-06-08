plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    // KSP (with argot-processor) is wired in Milestone 5, when the annotation command is added.
}

kotlin {
    jvmToolchain(17)
}

// The sample demonstrates BOTH consumer styles and doubles as an end-to-end integration test.
// It is deliberately NOT published (no maven-publish plugin here).
dependencies {
    implementation(project(":argot-core"))
    implementation(project(":argot-annotations"))

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.draftcode.argot.sample.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
