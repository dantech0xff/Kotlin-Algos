plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    jvmToolchain(20)
    jvm()
    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core)
        }
    }
}
