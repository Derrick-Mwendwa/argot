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

// argot-annotations holds only the annotation declarations consumed by the KSP processor.
// Constraint: zero third-party runtime dependencies — stdlib only.
dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
