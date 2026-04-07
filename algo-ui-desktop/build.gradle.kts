plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    jvmToolchain(20)
}

dependencies {
    implementation(project(":algo-shared"))
    implementation(project(":algo-core"))
    implementation(project(":algo-viz-engine"))
    implementation(project(":algo-ui-shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(compose.materialIconsExtended)
}

compose.desktop {
    application {
        mainClass = "com.thealgorithms.ui.MainKt"
    }
}
