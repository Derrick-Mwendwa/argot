plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    application
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":argot-core"))
    implementation(project(":argot-annotations"))
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
