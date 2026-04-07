plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    jvmToolchain(20)
    jvm()
    wasmJs { browser() }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":algo-shared"))
            implementation(project(":algo-core"))
            implementation(project(":algo-viz-engine"))
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(libs.coroutines.core)
        }
    }
}
