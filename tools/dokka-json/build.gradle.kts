plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

// A build tool, never published and carrying no ABI validation.
//
// Dokka is compileOnly because the plugin loads into Dokka's own worker classpath, which already
// supplies these classes; bundling them puts two copies of dokka-core in front of the service
// loader and the extension points stop matching.
dependencies {
    compileOnly(libs.dokka.core)
    compileOnly(libs.dokka.base)

    testImplementation(libs.dokka.core)
    testImplementation(libs.dokka.base)
    testImplementation(platform(libs.junit.bom))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
