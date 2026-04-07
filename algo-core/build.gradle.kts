plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    jvmToolchain(20)
}

dependencies {
    implementation(project(":algo-shared"))
    api(libs.kotlin.stdlib)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
