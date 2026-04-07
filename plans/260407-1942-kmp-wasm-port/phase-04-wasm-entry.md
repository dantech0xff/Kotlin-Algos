# Phase 04: Wasm Browser Entry — algo-ui-web Module

**Date:** 2026-04-07
**Priority:** P1
**Status:** [x] Complete

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: Phase 03 (shared UI module)

## Overview

Create `algo-ui-web` module with Kotlin/Wasm Compose entry point. Browser-hosted algorithm visualizer using shared UI components from `algo-ui-shared`.

## Key Insights

- Kotlin/Wasm uses `wasmJs` target — not `wasm` (Gradle convention)
- Compose Multiplatform Wasm entry uses `BrowserViewportWindow` (Compose 1.7+)
- Browser keyboard events map to our `VizKey` abstraction
- Output is a static site (HTML + JS + Wasm) — perfect for GitHub Pages
- `kotlinx.browser` package provides `window`, `document` APIs for Wasm

## Requirements

### Functional
- New `algo-ui-web` module with `wasmJs` target
- `main.kt` entry point using Compose Wasm browser API
- `index.html` template with required script/Wasm loading
- Keyboard event handling via browser `KeyboardEvent` → `VizKey`
- App renders in browser at `http://localhost:8080` during dev
- Production build outputs static files for GitHub Pages

### Non-Functional
- First meaningful paint < 2s on modern browser
- Wasm binary < 5MB
- Works on Chrome, Firefox, Safari (latest 2 versions)

## Architecture

### Module Structure
```
algo-ui-web/
├── build.gradle.kts
└── src/
    └── wasmJsMain/
        ├── kotlin/
        │   └── com/thealgorithms/ui/web/
        │       └── Main.kt
        └── resources/
            └── index.html
```

### Build File
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "algo-viz.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":algo-shared"))
            implementation(project(":algo-core"))
            implementation(project(":algo-viz-engine"))
            implementation(project(":algo-ui-shared"))
            implementation(compose.html.core)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(libs.coroutines.core)
        }
    }
}
```

### Entry Point (Main.kt)
```kotlin
package com.thealgorithms.ui.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.thealgorithms.ui.AppContent
import com.thealgorithms.ui.AppViewModel
import com.thealgorithms.ui.model.VizKey
import org.jetbrains.compose.web.renderComposableInBody

fun main() {
    renderComposableInBody {
        WebApp()
    }
}

@Composable
fun WebApp() {
    val viewModel = remember { AppViewModel() }

    // Browser keyboard handling
    // Use kotlinx.browser.window for key events
    // Map browser KeyboardEvent to VizKey

    AppContent(
        viewModel = viewModel,
        onKeyEvent = { vizKey ->
            when (vizKey) {
                VizKey.PLAY_PAUSE -> viewModel.play()
                VizKey.STEP_FORWARD -> viewModel.stepForward()
                VizKey.STEP_BACK -> viewModel.stepBack()
                VizKey.RESET -> viewModel.stop()
            }
        }
    )
}
```

**Note:** Exact Compose Wasm API may differ. With Compose Multiplatform 1.7+, the recommended approach is:
```kotlin
// Compose Multiplatform 1.7+ with full Canvas support
fun main() {
    CanvasBasedWindow(title = "Algorithm Visualizer") {
        val viewModel = remember { AppViewModel() }
        AppContent(viewModel = viewModel, onKeyEvent = { /* map keys */ })
    }
}
```

### index.html
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Algorithm Visualizer — Kotlin/Wasm</title>
    <style>
        html, body { margin: 0; padding: 0; height: 100%; overflow: hidden; }
        #root { width: 100%; height: 100%; }
    </style>
</head>
<body>
    <canvas id="ComposeTarget"></canvas>
    <script src="algo-viz.js"></script>
</body>
</html>
```

## Related Code Files

### Create
- `algo-ui-web/build.gradle.kts`
- `algo-ui-web/src/wasmJsMain/kotlin/com/thealgorithms/ui/web/Main.kt`
- `algo-ui-web/src/wasmJsMain/resources/index.html`

### Modify
- `settings.gradle.kts` — add `include("algo-ui-web")`

### Read (reference)
- `algo-ui-desktop/src/main/kotlin/com/thealgorithms/ui/Main.kt` — for AppContent pattern

## Implementation Steps

1. Create `algo-ui-web/build.gradle.kts` with wasmJs target + compose
2. Create `index.html` in `wasmJsMain/resources/`
3. Create `Main.kt` with browser entry point + keyboard mapping
4. Update `settings.gradle.kts` to include `algo-ui-web`
5. Verify: `./gradlew :algo-ui-web:wasmJsBrowserRun --continuously` opens browser
6. Test keyboard shortcuts in browser
7. Verify visualization renders correctly

## Todo List

- [x] Create `algo-ui-web/build.gradle.kts`
- [x] Create `index.html` resource
- [x] Create `Main.kt` entry point
- [x] Update `settings.gradle.kts`
- [x] Verify browser run works
- [x] Test keyboard shortcuts
- [x] Test visualization rendering

## Success Criteria
- [x] `./gradlew :algo-ui-web:wasmJsBrowserRun` opens browser showing visualizer
- [x] All 8 algorithms selectable and runnable in browser
- [x] Keyboard shortcuts work (Space, arrows, R)
- [x] Wasm binary < 5MB
- [x] Works on Chrome + Firefox

## Risk Assessment
| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Compose Wasm Canvas rendering differences | Medium | Medium | Test side-by-side with Desktop |
| Browser keyboard event differences | Low | Low | Abstract via VizKey, test on multiple browsers |
| Wasm binary size too large | Medium | Low | Monitor size, strip unused deps if needed |
| `CanvasBasedWindow` API not available | Medium | High | Check exact Compose 1.7 Wasm API — may use `renderComposableInBody` instead |

## Security Considerations
- None (static client-side app)

## Next Steps
- Phase 06 (GitHub Pages) deploys this module
