# Phase 05: Desktop Update — Slim Down to Use Shared UI

**Date:** 2026-04-07
**Priority:** P1
**Status:** [x] Complete

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: Phase 03 (shared UI extracted)

## Overview

Update `algo-ui-desktop` to depend on `:algo-ui-shared` instead of containing its own UI components. Desktop module becomes a thin entry point: window management + JVM keyboard mapping only.

## Key Insights

- After Phase 03, all Compose components live in `algo-ui-shared`
- Desktop module only needs: `main()` function, `Window` composable, JVM `Dimension`, JVM `KeyEvent` → `VizKey` mapping
- This is a **slim-down**, not a rewrite — behavior unchanged

## Requirements

### Functional
- `algo-ui-desktop` depends on `:algo-ui-shared` (replaces local component imports)
- `Main.kt` only contains: `main()`, window config, JVM keyboard mapping
- All local component files deleted (now in shared module)
- Desktop app launches and behaves identically to pre-migration

## Architecture

### Desktop Main.kt (after)
```kotlin
package com.thealgorithms.ui

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.thealgorithms.ui.shared.AppContent
import com.thealgorithms.ui.shared.AppViewModel
import com.thealgorithms.ui.shared.model.VizKey
import java.awt.Dimension
import java.awt.event.KeyEvent

fun main() = application {
    val viewModel = remember { AppViewModel() }

    Window(
        onCloseRequest = {
            viewModel.destroy()
            exitApplication()
        },
        title = "Algorithm Visualizer",
        state = rememberWindowState(width = 1200.dp, height = 800.dp),
        onPreviewKeyEvent = { keyEvent ->
            when {
                keyEvent.type == androidx.compose.ui.input.key.KeyEventType.KeyDown -> {
                    val vizKey = when (keyEvent.awtEventOrNull?.keyCode) {
                        KeyEvent.VK_SPACE -> VizKey.PLAY_PAUSE
                        KeyEvent.VK_RIGHT -> VizKey.STEP_FORWARD
                        KeyEvent.VK_LEFT -> VizKey.STEP_BACK
                        KeyEvent.VK_R -> VizKey.RESET
                        else -> null
                    }
                    vizKey?.let { viewModel.onKey(it); true } ?: false
                }
                else -> false
            }
        }
    ) {
        window.minimumSize = Dimension(1200, 800)
        androidx.compose.material3.MaterialTheme {
            AppContent(viewModel)
        }
    }
}
```

### Build File Update
```kotlin
dependencies {
    implementation(project(":algo-shared"))
    implementation(project(":algo-core"))
    implementation(project(":algo-viz-engine"))
    implementation(project(":algo-ui-shared"))  // NEW — replaces local components
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
}
```

## Related Code Files

### Modify
- `algo-ui-desktop/build.gradle.kts` — add `:algo-ui-shared` dependency
- `algo-ui-desktop/src/main/kotlin/com/thealgorithms/ui/Main.kt` — slim down to entry-only

### Delete (moved to algo-ui-shared in Phase 03)
- `algo-ui-desktop/src/main/kotlin/com/thealgorithms/ui/AppViewModel.kt`
- `algo-ui-desktop/src/main/kotlin/com/thealgorithms/ui/model/AlgorithmRegistry.kt`
- `algo-ui-desktop/src/main/kotlin/com/thealgorithms/ui/components/NavigationPanel.kt`
- `algo-ui-desktop/src/main/kotlin/com/thealgorithms/ui/components/SortVisualization.kt`
- `algo-ui-desktop/src/main/kotlin/com/thealgorithms/ui/components/SearchVisualization.kt`
- `algo-ui-desktop/src/main/kotlin/com/thealgorithms/ui/components/PlaybackControls.kt`
- `algo-ui-desktop/src/main/kotlin/com/thealgorithms/ui/components/InputConfigPanel.kt`
- `algo-ui-desktop/src/main/kotlin/com/thealgorithms/ui/components/StatsPanel.kt`

## Implementation Steps

1. Update `algo-ui-desktop/build.gradle.kts` — add `:algo-ui-shared` dep
2. Rewrite `Main.kt` to use shared imports + JVM key mapping
3. Delete all moved component/model/viewmodel files
4. Verify: `./gradlew :algo-ui-desktop:run` launches correctly
5. Test all algorithms, playback controls, keyboard shortcuts

## Todo List
- [x] Update `build.gradle.kts` with shared dependency
- [x] Rewrite `Main.kt` as thin entry point
- [x] Delete moved files
- [x] Verify desktop app runs
- [x] Test all 8 algorithms
- [x] Test keyboard shortcuts

## Success Criteria
- [x] `./gradlew :algo-ui-desktop:run` launches correctly
- [x] All 8 algorithms visualizable
- [x] Keyboard shortcuts work (Space, arrows, R)
- [x] Desktop module has < 50 lines of Compose code (just entry point)

## Risk Assessment
| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Import path changes break build | Medium | Low | IDE auto-corrects, verify with gradlew build |
| JVM key mapping differs from current behavior | Low | Low | Side-by-side test before/after |

## Security Considerations
- None

## Next Steps
- Phase 04 (Wasm entry) and this phase can run in parallel
