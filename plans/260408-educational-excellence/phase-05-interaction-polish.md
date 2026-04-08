# Phase 05: Interaction Polish

## Context
- Parent: [plan.md](./plan.md)
- Depends on: [phase-04-layout-integration.md](./phase-04-layout-integration.md)
- Brainstorm: [brainstorm-260408-educational-excellence.md](../reports/brainstorm-260408-educational-excellence.md)

## Overview
- **Date**: 2026-04-08
- **Priority**: P2
- **Status**: Completed
- **Effort**: 2.5h

Polish interactions: input presets, progress scrubber, sidebar hover + search filter, completion celebration, index labels, keyboard hints.

## Key Insights
- InputConfigPanel only has "Random" + custom text — missing educational presets
- No progress scrubber — students can't seek to arbitrary events
- Sidebar hover defined but never applied
- SortVisualization has no completion celebration
- No index labels on bars/cells
- PlaybackControls has no keyboard shortcut hints

## Requirements
- Input presets: Nearly Sorted, Reversed, All Equal, Many Duplicates (in addition to Random)
- Progress scrubber: slider mapping to event index, drag to seek
- Sidebar hover: apply existing SidebarHoverBg on pointer hover
- Sidebar search: text filter at top to search algorithms by name
- Completion celebration: rainbow gradient sweep when PlaybackState.Complete
- Index labels: show index below bars (≤25 elements) and inside cells
- Keyboard hints: small muted text below playback buttons

## Architecture

### Input Presets
```kotlin
enum class InputPreset(val label: String) {
    RANDOM("Random"),
    NEARLY_SORTED("Nearly Sorted"),
    REVERSED("Reversed"),
    ALL_EQUAL("All Equal"),
    MANY_DUPLICATES("Many Duplicates")
}

fun generatePreset(preset: InputPreset, size: Int): List<Int> = when (preset) {
    RANDOM -> List(size) { (1..99).random() }
    NEARLY_SORTED -> {
        val sorted = (1..size).toList()
        // Swap 1-2 random pairs
        val arr = sorted.toMutableList()
        repeat(minOf(2, size / 5)) {
            val i = (0 until size).random()
            val j = (0 until size).random()
            arr[i] = arr[j].also { arr[j] = arr[i] }
        }
        arr.toList()
    }
    REVERSED -> (size downTo 1).toList()
    ALL_EQUAL -> List(size) { 42 }
    MANY_DUPLICATES -> List(size) { listOf(1, 3, 5, 7, 9).random() }
}
```

### Progress Scrubber
Add `seekTo(index: Int)` to AlgorithmPlayer + AppViewModel:
```kotlin
// AlgorithmPlayer.kt
fun seekTo(index: Int) {
    playbackJob?.cancel()
    val clamped = index.coerceIn(0, buffer.size - 1)
    _currentEventIndex.value = clamped
    _currentSnapshot.value = reconstructToIndex(clamped)
    _state.value = PlaybackState.Paused
}

// AppViewModel.kt
fun seekTo(index: Int) = player.seekTo(index)
```

Slider in PlaybackControls:
```kotlin
Slider(
    value = progress.first.toFloat(),
    onValueChange = { onSeek(it.toInt()) },
    valueRange = 0f..progress.second.toFloat(),
    steps = progress.second - 1  // discrete steps
)
```

### Sidebar Hover
Use `pointerHoverIcon` + `pointerInput` or `Modifier.pointerHoverFilter`:
```kotlin
var isHovered by remember { mutableStateOf(false) }
Box(
    modifier = Modifier
        .background(if (isHovered) VizColors.sidebarHoverBg else Color.Transparent)
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    isHovered = event.type == PointerEventType.Move
                }
            }
        }
)
```

### Sidebar Search Filter
```kotlin
var filterText by remember { mutableStateOf("") }
TextField(
    value = filterText,
    onValueChange = { filterText = it },
    placeholder = { Text("Search algorithms...") },
    modifier = Modifier.fillMaxWidth().padding(8.dp)
)
val filteredAlgorithms = algorithms.filter {
    it.name.contains(filterText, ignoreCase = true)
}
```

### Completion Celebration
In SortVisualization, when `sortedIndices.size == arrayState.size` (all sorted):
```kotlin
// Rainbow gradient sweep
val rainbowOffset by animateFloatAsState(
    targetValue = if (isComplete) 1f else 0f,
    animationSpec = tween(durationMillis = 1500)
)
// For each bar, check if its normalized position < rainbowOffset
// If so, apply rainbow color instead of sorted green
fun rainbowColor(normalizedPos: Float): Color {
    val hue = (normalizedPos * 360f)
    return Color(ColorUtils.HSLToColor(floatArrayOf(hue, 0.8f, 0.6f)))
}
```

### Index Labels
SortVisualization (below each bar, when ≤25 elements):
```kotlin
if (arrayState.size <= 25) {
    drawText(
        textMeasurer.measure("${index}"),
        topLeft = Offset(barX + barWidth / 2 - textWidth / 2, barBottom + 4.dp.toPx()),
        color = VizColors.textMuted
    )
}
```

SearchVisualization (inside each cell):
```kotlin
// Inside cell composable
Text("${index}", fontSize = 9.sp, color = VizColors.textMuted)
```

### Keyboard Hints
```kotlin
Row(horizontalArrangement = Arrangement.SpaceEvenly) {
    HintButton(icon = Icons.Default.PlayArrow, hint = "Space")
    HintButton(icon = Icons.Default.SkipNext, hint = "→")
    HintButton(icon = Icons.Default.SkipPrevious, hint = "←")
    HintButton(icon = Icons.Default.Replay, hint = "R")
}
```

## Related Code Files

### Modify
| File | Change |
|------|--------|
| `algo-ui-shared/.../ui/components/InputConfigPanel.kt` | Add preset buttons, InputPreset enum |
| `algo-ui-shared/.../ui/components/PlaybackControls.kt` | Add scrubber slider + keyboard hints |
| `algo-ui-shared/.../ui/components/NavigationPanel.kt` | Add hover effect + search filter |
| `algo-ui-shared/.../ui/components/SortVisualization.kt` | Add completion celebration + index labels |
| `algo-ui-shared/.../ui/components/SearchVisualization.kt` | Add index labels |
| `algo-ui-shared/.../ui/AppViewModel.kt` | Add seekTo() |
| `algo-viz-engine/.../AlgorithmPlayer.kt` | Add seekTo() |
| `algo-ui-shared/.../ui/AppContent.kt` | Wire seekTo through |

## Implementation Steps

1. **Add seekTo to AlgorithmPlayer** — Random-access event seeking
2. **Add seekTo to AppViewModel** — Delegate to player
3. **Add progress scrubber to PlaybackControls** — Slider + seek callback
4. **Add input presets to InputConfigPanel** — Preset enum + buttons + generator function
5. **Add sidebar hover + search filter** — Pointer events + text field
6. **Add completion celebration to SortVisualization** — Rainbow sweep
7. **Add index labels** — Sort bars + search cells
8. **Add keyboard hints** — Muted text below playback buttons
9. **Wire everything in AppContent** — SeekTo callback, preset callback
10. **Run all tests + visual verification**

## Todo
- [ ] Add seekTo() to AlgorithmPlayer
- [ ] Add seekTo() to AppViewModel
- [ ] Add progress scrubber to PlaybackControls
- [ ] Add InputPreset enum + generator
- [ ] Add preset buttons to InputConfigPanel
- [ ] Add hover effect to NavigationPanel
- [ ] Add search filter to NavigationPanel
- [ ] Add completion celebration to SortVisualization
- [ ] Add index labels to SortVisualization
- [ ] Add index labels to SearchVisualization
- [ ] Add keyboard hints to PlaybackControls
- [ ] Wire new callbacks in AppContent
- [ ] End-to-end visual verification
- [ ] Run all tests

## Success Criteria
- [ ] Seek scrubber jumps to any event instantly
- [ ] Input presets generate correct array patterns
- [ ] Sidebar items highlight on mouse hover
- [ ] Search filter narrows algorithm list in real-time
- [ ] Completion shows rainbow sweep animation
- [ ] Index labels visible on bars (≤25) and cells
- [ ] Keyboard hints visible below playback buttons
- [ ] All existing tests pass

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| Scrubber jank on large event counts | Slider steps = discrete, no continuous dragging |
| Rainbow animation performance | Only triggers once on Complete, Canvas-native |
| Search filter flicker | Use derivedStateOf for filtered list |
| Pointer hover API differences Desktop/Wasm | Test on both platforms |

## Security Considerations
None.

## Next Steps
- Final QA pass on both Desktop + Web (Wasm) platforms
- Update README with new features
- Deploy updated version to GitHub Pages
