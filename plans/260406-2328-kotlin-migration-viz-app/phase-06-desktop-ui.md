# Phase 06: Desktop UI — Compose Desktop App with Visualization Controls

**Date:** 2026-04-07
**Priority:** P1
**Status:** [ ] Pending

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: Phase 03 (viz engine), Phase 04 (sorting algos), Phase 05 (search algos)
- Research: [researcher-01-compose-viz-architecture.md](research/researcher-01-compose-viz-architecture.md)

## Overview

Build the Compose Desktop application shell with sidebar navigation, visualization canvas, playback controls, code viewer panel, and input configuration. This is the user-facing layer that ties together the engine and algorithms.

## Key Insights

- Material 3 components: `NavigationRail` for sidebar, `TopAppBar`, `Slider`, `OutlinedTextField`
- Canvas API for bar chart visualization — custom `DrawScope` rendering
- `LaunchedEffect` bridges coroutines (engine) to Compose lifecycle
- `collectAsState()` converts `StateFlow` from engine to Compose state
- **[VALIDATED]** Code viewer with live execution pointer — highlight current source line during visualization. Requires event→source line mapping.
- Window size: 1200x800 minimum for comfortable layout

## Requirements

### Functional
- **Sidebar navigation**: List of algorithms (5 sorts + 3 searches), grouped by category
- **Visualization canvas**: Animated bar chart for sorts; array view with highlighted cells for searches
- **Playback controls**: Play/Pause, Stop, Step Forward, Step Back, Speed slider
- **Input configuration**: Text field for custom array input, "Random" button for random arrays, array size spinner
- **Code viewer**: Panel showing algorithm source code with live execution pointer — highlights current line matching active event
- **Stats panel**: Comparisons count, swaps count, time elapsed, current event index
- **Responsive layout**: Sidebar collapses on narrow windows

### Non-Functional
- 60fps rendering for up to 200 bar elements
- Smooth transitions between events (animate bar heights, color changes)
- Keyboard shortcuts: Space=play/pause, Right=step forward, Left=step back, R=reset
- Dark theme support (Material 3 dynamic theming)

## Architecture

### UI Component Tree

```
MainWindow (1200x800)
├── NavigationRail (sidebar, 80dp width)
│   ├── CategoryHeader("Sorting")
│   ├── AlgorithmButton("BubbleSort")
│   ├── AlgorithmButton("SelectionSort")
│   ├── ...
│   ├── CategoryHeader("Search")
│   ├── AlgorithmButton("LinearSearch")
│   └── ...
├── MainContent (remaining width)
│   ├── TopAppBar (algorithm name + stats)
│   ├── VisualizationCanvas (center, flexible height)
│   │   ├── SortVisualization (bar chart)
│   │   └── SearchVisualization (cell grid with highlights)
│   ├── PlaybackControls (bottom bar)
│   │   ├── PlayPauseButton
│   │   ├── StopButton
│   │   ├── StepForwardButton
│   │   ├── StepBackButton
│   │   ├── SpeedSlider
│   │   └── ProgressIndicator (event X / total)
│   └── BottomPanel (optional: code viewer + input config, tabbed)
│       ├── InputConfigTab
│       └── CodeViewerTab
```

### State Management

```kotlin
// Single ViewModel-like state holder
class AppViewModel(private val player: AlgorithmPlayer) {
    val playbackState: StateFlow<PlaybackState> = player.state
    val snapshot: StateFlow<AlgorithmSnapshot> = player.currentSnapshot
    val eventProgress: StateFlow<Pair<Int, Int>> = player.currentEventIndex.combine(...)
    val selectedAlgorithm: MutableStateFlow<AlgorithmInfo>
    val inputArray: MutableStateFlow<List<Int>>
}
```

### Data Flow

```
User selects algorithm → AppViewModel.selectedAlgorithm updates
User configures input → AppViewModel.inputArray updates
User clicks Play → AppViewModel calls player.run(algo, input)
    ↓
AlgorithmPlayer executes → emits events → builds snapshots
    ↓
snapshot.collectAsState() → Compose recomposition → Canvas redraws
    ↓
Stats update via eventProgress.collectAsState()
```

## Related Code Files

### Create (algo-ui-desktop)
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/Main.kt` — entry point, window config
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/AppViewModel.kt` — state management
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/theme/Theme.kt` — Material 3 theme
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/components/NavigationPanel.kt` — sidebar
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/components/VisualizationCanvas.kt` — main viz area
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/components/SortVisualization.kt` — bar chart renderer
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/components/SearchVisualization.kt` — cell grid renderer
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/components/PlaybackControls.kt` — transport bar
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/components/InputConfigPanel.kt` — array input
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/components/StatsPanel.kt` — counters
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/components/CodeViewerPanel.kt` — source display with execution pointer
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/model/SourceLineMapper.kt` — maps AlgorithmEvent types to source code line numbers
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/model/AlgorithmRegistry.kt` — algorithm catalog

### Modify
- `algo-ui-desktop/build.gradle.kts` — add dependencies on all modules

### Delete
- Placeholder `Main.kt` from Phase 01 (replaced with full version)

## Implementation Steps

1. Create `Theme.kt` — Material 3 dark/light theme with custom color scheme
2. Create `AlgorithmRegistry.kt` — catalog of available algorithms with metadata (name, category, description, factory)
3. Create `AppViewModel.kt` — state holder bridging UI and `AlgorithmPlayer`
4. Update `Main.kt` — replace placeholder with `MainWindow` composable, window config (1200x800, title)
5. Create `NavigationPanel.kt` — `NavigationRail` with algorithm list, grouped by category
6. Create `VisualizationCanvas.kt` — container that delegates to sort/search visualizer based on algorithm type
7. Create `SortVisualization.kt` — Canvas-based bar chart: bar height = value, color = highlighted state
8. Create `SearchVisualization.kt` — grid of cells, highlight probed/found/not-found cells
9. Create `PlaybackControls.kt` — row of icon buttons + speed slider + progress indicator
10. Create `InputConfigPanel.kt` — text field + random button + size spinner
11. Create `StatsPanel.kt` — comparisons, swaps, time, event progress
12. Create `CodeViewerPanel.kt` — scrollable text with monospace font + current-line highlight
13. Create `SourceLineMapper.kt` — maps event types (Compare→line X, Swap→line Y) per algorithm for execution pointer
14. Add keyboard shortcuts to `MainWindow`
13. Add keyboard shortcuts to `MainWindow`
14. Wire everything: selection → input → play → visualization → stats
15. Verify: `./gradlew :algo-ui-desktop:run` launches fully functional app

## Todo List

- [ ] Create `Theme.kt`
- [ ] Create `AlgorithmRegistry.kt`
- [ ] Create `AppViewModel.kt`
- [ ] Update `Main.kt`
- [ ] Create `NavigationPanel.kt`
- [ ] Create `VisualizationCanvas.kt`
- [ ] Create `SortVisualization.kt`
- [ ] Create `SearchVisualization.kt`
- [ ] Create `PlaybackControls.kt`
- [ ] Create `InputConfigPanel.kt`
- [ ] Create `StatsPanel.kt`
- [ ] Create `CodeViewerPanel.kt`
- [ ] Add keyboard shortcuts
- [ ] Wire all components end-to-end
- [ ] Verify `./gradlew :algo-ui-desktop:run` works

## Success Criteria

- [ ] App launches with sidebar showing 8 algorithms (5 sort + 3 search)
- [ ] Selecting BubbleSort + clicking Play shows animated bar chart sorting `[5,3,1,4,2]`
- [ ] Step Forward/Back correctly advances/rewinds visualization
- [ ] Speed slider changes animation speed visibly
- [ ] Stats panel shows correct comparison/swap counts
- [ ] LinearSearch visualization highlights probed cells sequentially
- [ ] BinarySearch visualization shows range narrowing
- [ ] Keyboard shortcuts work (Space, Left, Right, R)
- [ ] App runs without errors for 2+ minutes of continuous use

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Canvas rendering lag with 200+ bars | Medium | Medium | Use `remember` for computed values; `drawWithCache` for static elements |
| Compose recomposition storms from rapid StateFlow emissions | Medium | High | Batch events in engine (10ms min interval); use `distinctUntilChanged` |
| Layout breaks on different screen sizes | Low | Low | Use `BoxWithConstraints` for responsive layout |
| Dark/light theme inconsistencies | Low | Low | Test both; use Material 3 color tokens, not hardcoded colors |

## Security Considerations

- None (desktop app, no network access)

## Next Steps

- Phase 07 (Testing & Polish) adds comprehensive UI tests and documentation
