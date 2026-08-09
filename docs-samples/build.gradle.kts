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

// The landing page's hero shows the same program declared both ways beside the help Argot generates
// from it. All three come from here rather than from hand-written page copy, so they cannot disagree
// with the library.
val heroSources = listOf("Greet.kt", "GreetCommand.kt").map {
    layout.projectDirectory.file("src/main/kotlin/org/draftcode/argot/samples/$it")
}
val heroOutput = layout.buildDirectory.file("hero/hero.json")

val heroData by tasks.registering(JavaExec::class) {
    group = "documentation"
    description = "Emits the landing page hero panels from the real samples and parser."
    mainClass.set("org.draftcode.argot.samples.HeroDataKt")
    classpath = sourceSets["main"].runtimeClasspath

    // The published coordinates on the page have to name the version the samples were built
    // against, which is the release when -PargotVersion is set and the project version otherwise.
    val heroVersion = argotVersion ?: project.version.toString()
    inputs.files(heroSources)
    inputs.property("version", heroVersion)
    outputs.file(heroOutput)
    argumentProviders.add(
        CommandLineArgumentProvider {
            heroSources.map { it.asFile.absolutePath } +
                listOf(heroVersion, heroOutput.get().asFile.absolutePath)
        }
    )
}

tasks.named("build") { dependsOn(heroData) }
