# Phase 03: Shared UI Module — Extract Compose UI to algo-ui-shared

**Date:** 2026-04-07
**Priority:** P1
**Status:** [x] Complete

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: Phase 02 (restructured core modules)

## Overview

Create new `algo-ui-shared` module with Compose Multiplatform. Extract all platform-agnostic Compose UI components from `algo-ui-desktop` into shared `commonMain` source set.

## Key Insights

- Current `algo-ui-desktop` contains 9 files. Only `Main.kt` has JVM-specific code (`java.awt.*` imports, `Window`, `application`).
- `AppContent` composable is 100% platform-agnostic — pure Compose Material3 + Canvas
- All `components/*.kt` files are pure Compose — no JVM imports
- `AppViewModel` uses `kotlinx.coroutines` — KMP compatible
- `AlgorithmRegistry` imports from `algo-core` — KMP compatible (after Phase 02)
- Keyboard handling currently uses `java.awt.event.KeyEvent` → needs expect/actual pattern per platform

## JVM-Specific Code to Isolate

Only `Main.kt`:
```kotlin
import java.awt.Dimension                          // → JVM only
import java.awt.event.KeyEvent.VK_SPACE             // → JVM only
import androidx.compose.ui.window.Window             // → Desktop only
import androidx.compose.ui.window.application         // → Desktop only
import androidx.compose.ui.window.rememberWindowState // → Desktop only
```

## Requirements

### Functional
- New `algo-ui-shared` module with `kotlin("multiplatform")` + Compose Multiplatform
- Shared components: AppContent, NavigationPanel, SortVisualization, SearchVisualization, PlaybackControls, InputConfigPanel, StatsPanel
- Shared logic: AppViewModel, AlgorithmRegistry, AlgorithmCategory, AlgorithmInfo
- `AppContent` receives keyboard event callback as parameter (platform provides actual key mapping)
- All existing desktop behavior preserved

### Non-Functional
- Shared UI code in `commonMain` only
- No Compose Desktop or JVM imports in shared code

## Architecture

### New Module Structure
```
algo-ui-shared/
├── build.gradle.kts
└── src/
    └── commonMain/kotlin/com/thealgorithms/ui/
        ├── AppContent.kt          (shared composable layout)
        ├── AppViewModel.kt       (shared state management)
        ├── components/
        │   ├── NavigationPanel.kt
        │   ├── SortVisualization.kt
        │   ├── SearchVisualization.kt
        │   ├── PlaybackControls.kt
        │   ├── InputConfigPanel.kt
        │   └── StatsPanel.kt
        └── model/
            ├── AlgorithmRegistry.kt
            ├── AlgorithmCategory.kt
            └── AlgorithmInfo.kt
```

### Keyboard Abstraction
```kotlin
// In algo-ui-shared/commonMain
enum class VizKey { PLAY_PAUSE, STEP_FORWARD, STEP_BACK, RESET }

// Platform maps native keys → VizKey
// Desktop: java.awt.event.KeyEvent.VK_SPACE → VizKey.PLAY_PAUSE
// Web: Browser KeyboardEvent.code "Space" → VizKey.PLAY_PAUSE
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
    jvmToolchain(20)

    jvm()
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":algo-shared"))
            implementation(project(":algo-core"))
            implementation(project(":algo-viz-engine"))
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(libs.coroutines.core)
        }
    }
}
```

## Related Code Files

### Move (from algo-ui-desktop → algo-ui-shared/commonMain)
- `AppViewModel.kt` → `commonMain/.../ui/AppViewModel.kt`
- `model/AlgorithmRegistry.kt` → `commonMain/.../ui/model/AlgorithmRegistry.kt`
- `components/NavigationPanel.kt` → `commonMain/.../ui/components/NavigationPanel.kt`
- `components/SortVisualization.kt` → `commonMain/.../ui/components/SortVisualization.kt`
- `components/SearchVisualization.kt` → `commonMain/.../ui/components/SearchVisualization.kt`
- `components/PlaybackControls.kt` → `commonMain/.../ui/components/PlaybackControls.kt`
- `components/InputConfigPanel.kt` → `commonMain/.../ui/components/InputConfigPanel.kt`
- `components/StatsPanel.kt` → `commonMain/.../ui/components/StatsPanel.kt`

### Extract (from Main.kt)
- `AppContent` composable → `commonMain/.../ui/AppContent.kt`

### Create
- `algo-ui-shared/build.gradle.kts`
- `commonMain/.../ui/model/VizKey.kt` (keyboard abstraction)

### Modify
- `AppContent.kt` — replace `java.awt.event.KeyEvent` references with `VizKey` enum callback

## Implementation Steps

1. Create `algo-ui-shared/build.gradle.kts` with multiplatform + compose config
2. Create `VizKey.kt` enum for cross-platform keyboard abstraction
3. Move all component files from `algo-ui-desktop` → `algo-ui-shared/commonMain`
4. Move `AppViewModel.kt` and `AlgorithmRegistry.kt` to shared module
5. Extract `AppContent` from `Main.kt` → `AppContent.kt` in shared module
6. Update `AppContent` to accept `onKeyEvent: (VizKey) -> Unit` callback
7. Update `settings.gradle.kts` to include `algo-ui-shared`
8. Update `algo-ui-desktop/build.gradle.kts` to depend on `:algo-ui-shared`
9. Verify: `./gradlew :algo-ui-shared:build` compiles
10. Verify: `./gradlew :algo-ui-desktop:run` still works

## Todo List

- [x] Create `algo-ui-shared/build.gradle.kts`
- [x] Create `VizKey.kt` keyboard abstraction
- [x] Move component files to shared module
- [x] Move `AppViewModel` + `AlgorithmRegistry` to shared
- [x] Extract `AppContent` from `Main.kt`
- [x] Update `settings.gradle.kts`
- [x] Update `algo-ui-desktop` to depend on shared module
- [x] Verify shared module compiles
- [x] Verify desktop app still runs

## Success Criteria
- [x] `algo-ui-shared` compiles with zero JVM imports in `commonMain`
- [x] Desktop app runs with shared UI components
- [x] All Compose UI components accessible from both JVM and Wasm targets
- [x] `./gradlew :algo-ui-shared:build` succeeds

## Risk Assessment
| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Compose component behavior differs on Wasm | Medium | Medium | Test thoroughly on both platforms |
| StateFlow collectAsState differs on Wasm | Low | Low | Compose runtime handles this uniformly |
| Material3 component availability on Wasm | Low | Medium | Compose Multiplatform 1.7 includes full Material3 |

## Security Considerations
- None (UI code only)

## Next Steps
- Phase 04 (Wasm entry) + Phase 05 (Desktop update) can run in parallel
