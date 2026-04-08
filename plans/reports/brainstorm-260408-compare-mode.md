# Brainstorm: Compare Mode — 4 Algorithms Side-by-Side

**Date**: 2026-04-08
**Status**: Agreed
**Scope**: Sort algorithms only, 2×2 grid, synchronized playback

---

## Problem Statement

Students need to see how different sorting algorithms behave on the **same input at the same step**. Current single-algorithm view can't answer "why does QuickSort finish faster than BubbleSort?" visually.

## Agreed Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Playback mode | **Synchronized** — all 4 advance one event per tick | True comparison: "at step N, algo A is here, algo B is here" |
| Architecture | **Option A: Separate CompareViewModel** alongside AppViewModel | Zero risk to existing single-algorithm view |
| Scope | **Sort algorithms only** | Comparing sorts is the killer educational use case |
| Grid layout | **2×2 fixed** (exactly 4 slots) | Simple, symmetrical, no partial grids |
| Info panel | **No** in compare mode — mini stats per slot only | No room; full info via "Back to Single View" |
| Entry point | **"Compare" button** in sidebar header | Visible, intuitive |

## Architecture

### New Classes

| Class | Module | Purpose |
|-------|--------|---------|
| `CompareViewModel` | algo-ui-shared | Owns 4 `AlgorithmPlayer` instances, orchestrates synchronized playback |
| `CompareSlotState` | algo-ui-shared | Per-algorithm state: info, snapshot, event counts |
| `ComparePanel` | algo-ui-shared | 2×2 grid of mini visualizations |
| `CompareSlot` | algo-ui-shared | Single quadrant: header + mini SortVisualization + mini stats |
| `AlgorithmPickerDialog` | algo-ui-shared | Modal for selecting 4 algorithms from registry |
| `ViewMode` | algo-ui-shared | Enum: SINGLE, COMPARE |

### Layout

```
┌─────────┬──────────────────────────────────────────┐
│         │ Compare Mode          [← Back to Single]  │
│  Nav    │ ┌─────────────────┬────────────────────┐  │
│  Panel  │ │ 🔵 Bubble Sort  │ 🟣 Selection Sort  │  │
│  (200)  │ │ ████ ██ ██████  │ ██ ████ █████ ██   │  │
│         │ │ 12 cmp · 5 swp  │ 12 cmp · 3 swp     │  │
│         │ ├─────────────────┼────────────────────┤  │
│         │ │ 🟢 Quick Sort   │ 🟡 Insertion Sort  │  │
│         │ │ █████ ██ ████   │ ████ █████ ██ ████ │  │
│         │ │ 6 cmp · 2 swp   │ 8 cmp · 4 swp      │  │
│         │ └─────────────────┴────────────────────┘  │
│         │ Step 12/28  [◀ ⏵ ■ ▶] Speed: ███░░       │
│         │ Input: [5,3,1,4,2] [Random] [Presets]     │
└─────────┴──────────────────────────────────────────┘
```

### Synchronized Playback Logic

```kotlin
class CompareViewModel {
    private val slots = MutableList(4) { CompareSlotState() }
    private val players = List(4) { AlgorithmPlayer() }
    
    fun playAll() {
        scope.launch {
            while (slots.any { it.hasMoreEvents }) {
                slots.forEachIndexed { i, slot ->
                    if (slot.hasMoreEvents) players[i].stepForward()
                }
                delay(speedMs)
            }
        }
    }
}
```

- Some algorithms finish before others (different event counts)
- Each slot shows individual "Step X/Y" + comparisons + swaps
- Playback continues until ALL are complete
- Each slot gets a completion badge when done

### Entry: "Compare" Button in Sidebar

Add a button at top of NavigationPanel:
```
┌─────────────┐
│ ⚡ Compare   │  ← new button, switches to compare mode
├─────────────┤
│ 📊 Sorting   │
│  Bubble Sort │
│  Quick Sort  │
│  ...         │
└─────────────┘
```

Clicking it opens `AlgorithmPickerDialog` where user picks exactly 4 sort algorithms. Then switches `ViewMode` to COMPARE.

### AlgorithmPickerDialog

Modal dialog with:
- Grid of sort algorithm cards (11 available)
- User selects exactly 4 (checkboxes, max 4)
- "Start Compare" button disabled until 4 selected
- Pre-selected defaults: BubbleSort, SelectionSort, QuickSort, MergeSort
- "Cancel" to go back

### No Info Panel in Compare Mode

Each quadrant only shows:
- Algorithm name + colored header bar
- Mini SortVisualization (canvas, smaller bars)
- Mini stats: "12 comparisons · 5 swaps"
- "Step X/Y" counter
- Completion badge (✓ Done)

Full pseudocode + explanations available via "Back to Single View" on any algorithm.

## Files to Create/Modify

### Create
| File | Purpose |
|------|---------|
| `algo-ui-shared/.../ui/model/ViewMode.kt` | SINGLE / COMPARE enum |
| `algo-ui-shared/.../ui/CompareViewModel.kt` | 4-player synchronized orchestration |
| `algo-ui-shared/.../ui/model/CompareSlotState.kt` | Per-slot data class |
| `algo-ui-shared/.../ui/components/ComparePanel.kt` | 2×2 grid layout |
| `algo-ui-shared/.../ui/components/CompareSlot.kt` | Single quadrant component |
| `algo-ui-shared/.../ui/components/AlgorithmPickerDialog.kt` | Algorithm selection modal |

### Modify
| File | Change |
|------|--------|
| `algo-ui-shared/.../ui/AppContent.kt` | Add ViewMode switch: SINGLE → existing layout, COMPARE → ComparePanel |
| `algo-ui-shared/.../ui/components/NavigationPanel.kt` | Add "Compare" button at top |
| `algo-ui-shared/.../ui/components/SortVisualization.kt` | Support smaller size (compact mode for compare grid) |

**algo-viz-engine untouched** — `AlgorithmPlayer` reused as-is.

## Effort Estimate: ~8h

| Component | Effort |
|-----------|--------|
| CompareViewModel + CompareSlotState | 2h |
| ComparePanel (2×2 grid) + CompareSlot | 2h |
| AlgorithmPickerDialog | 1h |
| ViewMode toggle in AppContent + NavigationPanel | 1.5h |
| Shared input + playback controls | 1h |
| Testing + visual QA | 0.5h |

## Risks

| Risk | Mitigation |
|------|------------|
| 4 canvas renders = perf | Limit compare to ≤25 elements; smaller canvas |
| Different event counts confusing | Show per-slot step counter clearly |
| SortVisualization too small in quadrant | Compact mode: hide value labels, reduce gap/radius |
| Mobile layout impossible | Stack 2×1 on narrow screens, or disable compare on mobile |

## Success Criteria

- [ ] User can pick any 4 sort algorithms and see them run on same input
- [ ] All 4 advance one event per tick (synchronized)
- [ ] Each quadrant shows: name, bars, comparisons, swaps, step counter
- [ ] Compare mode doesn't break single-algorithm view
- [ ] AlgorithmPlayer reused without modification
- [ ] "Back to Single View" returns to normal mode

## Unresolved Questions
- None — all decisions agreed.
