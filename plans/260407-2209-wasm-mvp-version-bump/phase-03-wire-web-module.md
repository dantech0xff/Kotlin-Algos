# Phase 3: Wire Web Module

## Context

algo-ui-web has a placeholder shell. Dependencies are commented out. Main.kt renders "Coming soon". Must uncomment deps, add compose-compiler plugin, replace placeholder with AppContent(AppViewModel()) integration, and clean up.

## Overview

Transform algo-ui-web from placeholder to full visualizer by:
1. Updating `build.gradle.kts` — uncomment deps, add compose-compiler, remove experimental opt-in
2. Rewriting `Main.kt` — replace placeholder with AppContent(AppViewModel())
3. Updating `index.html` — minor tweaks for production
4. Updating `gradle.properties` — verify/remove experimental flag

## Requirements

- Must reference AppContent + AppViewModel from algo-ui-shared
- No keyboard handling in web (onKeyEvent param = null)
- CanvasBasedWindow for Compose Wasm rendering

## Implementation Steps

### Step 1: Update `algo-ui-web/build.gradle.kts`

**Replace entire file with:**
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

group = "com.thealgorithms"
version = "1.0.0"

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
            implementation(project(":algo-shared"))
            implementation(project(":algo-core"))
            implementation(project(":algo-viz-engine"))
            implementation(project(":algo-ui-shared"))
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(libs.coroutines.core)
        }
    }
}
```

**Changes from current:**
- Removed `import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl`
- Removed `@OptIn(ExperimentalWasmDsl::class)` (stable in Kotlin 2.1.x)
- Removed entire block comment (TODO explanations no longer needed)
- Uncommented all 4 `implementation(project(...))` lines
- Added `alias(libs.plugins.compose.compiler)`

### Step 2: Rewrite `algo-ui-web/src/wasmJsMain/kotlin/com/thealgorithms/ui/web/Main.kt`

**Replace entire file with:**
```kotlin
package com.thealgorithms.ui.web

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.thealgorithms.ui.AppContent
import com.thealgorithms.ui.AppViewModel

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "root", title = "Algorithm Visualizer") {
        MaterialTheme {
            AppContent(viewModel = AppViewModel())
        }
    }
}
```

**Changes from current:**
- Removed `PlaceholderApp()` composable
- Removed unused imports (Arrangement, Column, fillMaxSize, Surface, Text, Alignment, Modifier)
- Added imports for `AppContent` and `AppViewModel` from algo-ui-shared
- `onKeyEvent` param omitted (defaults to null — keyboard handling not critical for web MVP)

### Step 3: Verify `algo-ui-web/src/wasmJsMain/resources/index.html`

Current HTML is adequate. **No changes needed.** It already has:
- `<div id="root">` matching `canvasElementId = "root"`
- `<script src="algo-viz.js">` matching `outputFileName`
- Full-viewport styling with dark background

### Step 4: Update `gradle.properties`

**Current:**
```properties
org.jetbrains.compose.experimental.wasm.enabled=true
org.gradle.jvmargs=-Xmx2g
kotlin.daemon.jvmargs=-Xmx2g
```

**Replace with:**
```properties
org.gradle.jvmargs=-Xmx2g
kotlin.daemon.jvmargs=-Xmx2g
```

**Rationale**: At Compose 1.8.2 + Kotlin 2.1.20, wasmJs is no longer experimental. The flag may cause warnings or be unrecognized. Remove it.

### Step 5: Update `settings.gradle.kts` (no changes expected)

Current config already includes all modules and required repos. Verify no changes needed.

## Success Criteria

- `./gradlew :algo-ui-web:compileKotlinWasmJs` succeeds
- `./gradlew :algo-ui-web:wasmJsBrowserDistribution` succeeds
- Output at `algo-ui-web/build/dist/wasmJs/productionExecutable/` contains `index.html` + `algo-viz.js`
- `./gradlew :algo-ui-desktop:run` still works (no regression)

## Risk Assessment

- **Medium risk**: First time all modules wire together for wasmJs. Transitive dependency graph must resolve.
- **Common failure**: If an upstream module's `commonMain` uses JVM-only APIs, wasmJs compile will fail with clear error. All code is already in commonMain so this shouldn't happen.
- **`ExperimentalComposeUiApi`**: Still needed for `CanvasBasedWindow` at Compose 1.8.2. Safe to suppress.
- **Rollback**: Revert 3 files (build.gradle.kts, Main.kt, gradle.properties).
