plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
    jvm()
    wasmJs { browser() }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":algo-shared"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner)
            implementation(libs.kotest.assertions)
            implementation(libs.coroutines.core)
            implementation(libs.coroutines.test)
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
