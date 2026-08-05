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
        name.set("Argot Annotations")
        description.set("Annotations for Argot's annotation-style (KSP) argument parsing.")
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
