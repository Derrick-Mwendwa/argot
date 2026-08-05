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

dependencies {
    // No third-party runtime dependencies — stdlib only.
    testImplementation(platform(libs.junit.bom))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
