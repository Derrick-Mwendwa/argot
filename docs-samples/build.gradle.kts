plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(17)
}

// Every code block on the docs site is a region of a file in this module, so nothing on the site is
// hand-typed Kotlin that no compiler has seen.
//
// By default the samples build against the local project, which catches drift while you work. Pass
// -PargotVersion=0.1.2 to build them against that release from Maven Central instead — that is what
// verifies the published library actually behaves the way the docs say it does.
val argotVersion: String? = providers.gradleProperty("argotVersion").orNull

dependencies {
    if (argotVersion != null) {
        implementation("org.draftcode:argot-core:$argotVersion")
        implementation("org.draftcode:argot-annotations:$argotVersion")
        ksp("org.draftcode:argot-processor:$argotVersion")
    } else {
        implementation(project(":argot-core"))
        implementation(project(":argot-annotations"))
        ksp(project(":argot-processor"))
    }

    testImplementation(platform(libs.junit.bom))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
