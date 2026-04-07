import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
}

group = "com.thealgorithms"
version = "1.0.0"

/**
 * Wasm browser entry point for the Algorithm Visualizer.
 *
 * NOTE: The wasmJs target requires all transitive dependencies to also publish
 * wasmJs variants. Currently algo-shared, algo-core, algo-viz-engine, and
 * algo-ui-shared only declare jvm() targets. To fully enable Wasm:
 *   1. Add `wasmJs()` target to each upstream module's build.gradle.kts
 *   2. Move their source sets from jvmMain to commonMain (already done for most)
 *   3. Uncomment the project() dependencies below
 *   4. Replace Main.kt placeholder with full AppContent integration
 *
 * For now, this module compiles a minimal Compose Wasm shell to verify the
 * build pipeline works end-to-end.
 */
@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvmToolchain(20)

    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "algo-viz.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            // TODO: Uncomment once upstream modules add wasmJs() targets:
            // implementation(project(":algo-shared"))
            // implementation(project(":algo-core"))
            // implementation(project(":algo-viz-engine"))
            // implementation(project(":algo-ui-shared"))
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(libs.coroutines.core)
        }
    }
}
