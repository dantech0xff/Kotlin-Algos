# Phase 02: Compare UI Components

## Context
- Parent: [plan.md](./plan.md)
- Depends on: [phase-01-data-models-viewmodel.md](./phase-01-data-models-viewmodel.md)

## Overview
- **Date**: 2026-04-08
- **Priority**: P1
- **Status**: Pending
- **Effort**: 3h

Build 3 new UI components: AlgorithmPickerDialog, ComparePanel (2×2 grid), CompareSlot (single quadrant). Add compact mode to SortVisualization.

## Key Insights
- Each quadrant gets a mini SortVisualization — canvas-based, just smaller
- SortVisualization needs a `compact: Boolean` param to hide value labels, reduce gaps
- AlgorithmPickerDialog is a modal with checkboxes, max 4 selections
- ComparePanel is a 2×2 grid using Compose's `Row` + `Column`
- All use VizColors for consistent theme

## Requirements
- AlgorithmPickerDialog: select exactly 4 sort algorithms, pre-select defaults
- ComparePanel: 2×2 grid of CompareSlot components
- CompareSlot: algorithm name header + mini SortVisualization + mini stats
- SortVisualization: support compact mode (no value labels, tighter spacing)

## Architecture

### AlgorithmPickerDialog
```kotlin
@Composable
fun AlgorithmPickerDialog(
    sortAlgorithms: List<AlgorithmInfo>,
    preselected: List<AlgorithmInfo>,
    onConfirm: (List<AlgorithmInfo>) -> Unit,
    onDismiss: () -> Unit
)
```
- Modal dialog (AlertDialog or custom Dialog)
- Grid/list of algorithm cards with checkboxes
- Max 4 selections — disable unchecked items when 4 selected
- Pre-selected defaults: BubbleSort, SelectionSort, QuickSort, MergeSort
- "Start Compare" button enabled only when exactly 4 selected
- Dark theme, VizColors styling

### ComparePanel
```kotlin
@Composable
fun ComparePanel(
    slots: List<CompareSlotState>,
    playbackState: PlaybackState,
    speedMs: Long,
    progress: Pair<Int, Int>,
    inputArray: List<Int>,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onStepForward: () -> Unit,
    onStepBack: () -> Unit,
    onSpeedChange: (Long) -> Unit,
    onInputChange: (List<Int>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
)
```
- Full-screen layout replacing the center + right columns
- Top: "Compare Mode" header + [← Back to Single] button
- Middle: 2×2 grid of CompareSlot
- Bottom: shared PlaybackControls + InputConfigPanel
- No right info panel — too cramped

### CompareSlot
```kotlin
@Composable
fun CompareSlot(
    slotState: CompareSlotState,
    modifier: Modifier = Modifier
)
```
- Dark card with rounded corners
- Top: algorithm name as colored header (different color per slot for visual distinction)
- Center: mini SortVisualization(snapshot, compact = true)
- Bottom: mini stats row — "X comparisons · Y swaps · Step Z/W"
- Completion badge overlay when done: "✓ Complete"

### SortVisualization Compact Mode
Add `compact: Boolean = false` parameter:
- When compact: hide value labels above bars, hide index labels below
- When compact: reduce bar gap from adaptive to fixed 1px
- When compact: reduce corner radius
- When compact: smaller canvas doesn't need grid lines

Slot colors for 4 quadrants (visual distinction):
```kotlin
val slotColors = listOf(
    Color(0xFF60A5FA), // blue
    Color(0xFFA855F7), // purple
    Color(0xFF22C55E), // green
    Color(0xFFF97316), // orange
)
```

## Related Code Files

### Create
| File | Purpose |
|------|---------|
| `algo-ui-shared/.../ui/components/AlgorithmPickerDialog.kt` | Algorithm selection modal |
| `algo-ui-shared/.../ui/components/ComparePanel.kt` | 2×2 grid + controls |
| `algo-ui-shared/.../ui/components/CompareSlot.kt` | Single quadrant |

### Modify
| File | Change |
|------|--------|
| `algo-ui-shared/.../ui/components/SortVisualization.kt` | Add `compact: Boolean = false` param |

## Implementation Steps

1. Add `compact` parameter to SortVisualization
2. Create CompareSlot — header + mini viz + stats
3. Create ComparePanel — 2×2 grid layout + shared controls
4. Create AlgorithmPickerDialog — modal with checkboxes
5. Build verify

## Todo
- [ ] Add compact mode to SortVisualization
- [ ] Create CompareSlot.kt
- [ ] Create ComparePanel.kt
- [ ] Create AlgorithmPickerDialog.kt
- [ ] Build verify: `./gradlew :algo-ui-shared:compileKotlinWasmJs`

## Success Criteria
- [ ] All 3 new components compile
- [ ] SortVisualization compact mode hides labels/gridlines
- [ ] AlgorithmPickerDialog allows exactly 4 selections
- [ ] ComparePanel renders 2×2 grid with shared controls

## Next Steps
- Phase 03: Wire into AppContent with ViewMode toggle
