pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://dl.google.com/dl/android/maven2/")
        mavenCentral()
    }
}

rootProject.name = "kotlin-algos"
include("algo-shared")
include("algo-core")
include("algo-viz-engine")
include("algo-ui-shared")
include("algo-ui-web")
include("algo-ui-desktop")
