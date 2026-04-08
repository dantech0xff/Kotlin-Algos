# Phase 04: Three-Column Layout Integration

## Context
- Parent: [plan.md](./plan.md)
- Depends on: [phase-02-algorithm-descriptions.md](./phase-02-algorithm-descriptions.md) + [phase-03-info-panels.md](./phase-03-info-panels.md)
- Brainstorm: [brainstorm-260408-educational-excellence.md](../reports/brainstorm-260408-educational-excellence.md)

## Overview
- **Date**: 2026-04-08
- **Priority**: P1
- **Status**: Completed
- **Effort**: 1.5h

Wire everything together: three-column layout in AppContent, expose new state from AppViewModel, connect InfoPanel to real data.

## Key Insights
- Current layout: Row { NavigationPanel(240dp) | Column { StatsPanel + Viz + Controls + Input } }
- Target layout: Row { NavigationPanel(200dp) | Column { StatsPanel + Viz + Controls + Input } | InfoPanel(280dp) }
- Sidebar narrows from 240dp → 200dp to make room
- AppViewModel needs to expose: current step description, active pseudocode line, algorithm info
- StatsPanel simplifies — complexity moves to ComplexityPanel

## Requirements
- AppContent restructured to three-column Row
- InfoPanel visible when algorithm is selected, placeholder when not
- AppViewModel exposes new state flows for description + pseudocode highlight
- StatsPanel simplified (remove complexity from description, it's in ComplexityPanel now)
- InfoPanel data connected to ViewModel state

## Architecture

### Updated AppContent Layout
```kotlin
@Composable
fun AppContent(viewModel: AppViewModel, onKeyEvent: ((VizKey) -> Unit)? = null) {
    val selectedAlgo by viewModel.selectedAlgorithm.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val snapshot by viewModel.snapshot.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        // LEFT: Navigation (200dp)
        NavigationPanel(
            selectedAlgorithm = selectedAlgo,
            onAlgorithmSelected = { viewModel.selectAlgorithm(it) },
            modifier = Modifier.width(200.dp).fillMaxHeight()
        )

        // CENTER: Visualization area (flex)
        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
            StatsPanel(...)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // SortVisualization or SearchVisualization
            }
            LegendPanel(...)              // NEW: between viz and controls
            PlaybackControls(...)
            InputConfigPanel(...)
        }

        // RIGHT: Info Panel (280dp)
        if (selectedAlgo != null) {
            VerticalDivider(color = VizColors.divider)
            InfoPanel(
                algorithmInfo = selectedAlgo,
                snapshot = snapshot,
                playbackState = playbackState,
                progress = viewModel.progress.collectAsState().value,
                modifier = Modifier.width(280.dp).fillMaxHeight()
            )
        }
    }
}
```

### AppViewModel Changes
```kotlin
// New state flows
val currentStepDescription: StateFlow<String> =
    snapshot.map { it.currentDescription }
        .stateIn(scope, SharingStarted.Lazily, "")

val activePseudocodeLine: StateFlow<Int?> =
    snapshot.map { it.activePseudocodeLine }
        .stateIn(scope, SharingStarted.Lazily, null)
```

No need for separate flows — the data is already in `AlgorithmSnapshot` (added in Phase 01).

## Related Code Files

### Modify
| File | Change |
|------|--------|
| `algo-ui-shared/.../ui/AppContent.kt` | Three-column Row + InfoPanel + LegendPanel |
| `algo-ui-shared/.../ui/AppViewModel.kt` | Expose description/pseudocode from snapshot (if needed) |
| `algo-ui-shared/.../ui/components/StatsPanel.kt` | Simplify: remove complexity text from description line |
| `algo-ui-shared/.../ui/components/NavigationPanel.kt` | Width 240dp → 200dp |

## Implementation Steps

1. **Update NavigationPanel width** — 240dp → 200dp
2. **Update AppContent.kt** — Add third column with InfoPanel, add LegendPanel between viz and controls
3. **Simplify StatsPanel** — Description stays but complexity info moves to ComplexityPanel
4. **Wire InfoPanel** — Pass selectedAlgo, snapshot, playbackState, progress
5. **Test end-to-end** — Select algorithm → see pseudocode, descriptions, complexity, legend
6. **Run all tests**

## Todo
- [ ] Update NavigationPanel width to 200dp
- [ ] Restructure AppContent to three-column layout
- [ ] Add LegendPanel between visualization and playback controls
- [ ] Add InfoPanel as right column
- [ ] Simplify StatsPanel (remove redundant complexity info)
- [ ] End-to-end visual verification
- [ ] Run all tests

## Success Criteria
- [ ] Three-column layout renders correctly
- [ ] InfoPanel shows pseudocode, step explanation, complexity, legend
- [ ] Pseudocode line highlights in sync with visualization
- [ ] Step explanation updates on every event
- [ ] Legend visible without scrolling
- [ ] Navigation sidebar still functional
- [ ] All existing tests pass

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| 280dp info panel cramps visualization | Center column uses weight(1f) — fills remaining |
| InfoPanel content overflows vertically | Make InfoPanel scrollable |
| Missing description shows empty state | Default to "Select an algorithm" or "Running..." |

## Next Steps
- Phase 05 adds interaction polish (presets, scrubber, hover, celebration)
