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
    api(libs.coroutines.core)
}
