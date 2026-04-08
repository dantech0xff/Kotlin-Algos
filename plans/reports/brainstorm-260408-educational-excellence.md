# Brainstorm: Educational Excellence — Polish Current Algorithms

**Date**: 2026-04-08
**Status**: Agreed
**Scope**: 14 algorithms (11 sorts + 3 searches) — zero new algorithms, 100% polish

---

## Problem Statement

The Kotlin-Algos visualizer has 14 working algorithms with animated visualizations, but it's **educationally incomplete**. A student sees colored bars swapping but has no idea:
- **What** operation is happening (no step-by-step narrative)
- **Why** it's happening (no pseudocode with line highlighting)
- **What colors mean** (no legend)
- **How good** the algorithm is (no full complexity breakdown)

The goal: make every algorithm **self-explaining** — a student should be able to learn the algorithm from the tool alone, without external references.

---

## Architecture Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Step explanations | **Event-carried** — each `AlgorithmEvent` has `description: String` | Algorithm knows best what it's doing. Allows algorithm-specific language like "Pivot 5 is greater than 3" |
| Pseudocode format | `List<PseudocodeLine>` with `text: String` + `indentLevel: Int` | Simple enough to implement, structured enough for formatting/highlighting |
| Layout | **Three-column** (nav \| viz \| info panel) | See code + visualization simultaneously. VisuAlgo proven pattern |
| Scope | **All 3 phases** (A → B → C) | Each phase independently valuable, no inter-phase dependencies |

---

## Three-Column Layout Design

```
┌──────────┬────────────────────────────┬──────────────────────┐
│          │  StatsPanel (name, state)  │  Pseudocode Panel    │
│          ├────────────────────────────┤  ┌──────────────────┐ │
│  Nav     │                            │  │ 1 for i=0..n-1   │ │
│  Panel   │  Visualization             │  │ 2  for j=0..n-i  │ │
│  (200dp) │  (Sort/Search canvas)      │  │ 3   if a[j]>a[j+1]│ │
│          │                            │  │ 4    swap(j,j+1)  │ │
│          │                            │  └──────────────────┘ │
│          ├────────────────────────────┤  Step Explanation     │
│          │  PlaybackControls + Legend │  "Comparing index 2   │
│          ├────────────────────────────┤   (val=7) with index  │
│          │  InputConfigPanel          │   4 (val=1). Since    │
│          │  (presets + array input)   │   7>1, swap them."    │
│          │                            ├──────────────────────┤ │
│          │                            │  Complexity Info      │
│          │                            │  Best: O(n)           │
│          │                            │  Avg:  O(n²)          │
│          │                            │  Worst: O(n²)         │
│          │                            │  Space: O(1)          │
│          │                            │  Stable: Yes          │
│          │                            │  Difficulty: ★☆☆      │
└──────────┴────────────────────────────┴──────────────────────┘
```

Right panel width: **280dp** fixed. Left sidebar narrows from 240dp → 200dp to compensate.

---

## Phase A: "Understand" — Core Learning Features

### A1. Rich AlgorithmInfo Model

**File**: `algo-ui-shared/.../model/AlgorithmInfo.kt`

```kotlin
data class AlgorithmInfo(
    val name: String,
    val category: AlgorithmCategory,
    val description: String,
    val visualizer: Any,
    // NEW FIELDS:
    val difficulty: Difficulty,                    // BEGINNER, INTERMEDIATE, ADVANCED
    val timeComplexity: Complexity,               // best/avg/worst
    val spaceComplexity: String,                  // "O(1)", "O(n)", etc.
    val isStable: Boolean,
    val tags: List<String>,                       // "divide-and-conquer", "comparison-based", etc.
    val pseudocode: List<PseudocodeLine>,         // for code panel
)

data class Complexity(
    val best: String,       // "O(n)"
    val average: String,    // "O(n²)"
    val worst: String,      // "O(n²)"
)

data class PseudocodeLine(
    val text: String,
    val indentLevel: Int,   // 0, 1, 2, 3...
    val lineNumber: Int,    // 1-based
)

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }
```

**Effort**: Low. Data model change + populate 14 entries in `AlgorithmRegistry`.

### A2. Event Descriptions

**Files**: Every algorithm class in `algo-core/`, `AlgorithmEvent` in `algo-shared`

Add `description: String` parameter to relevant events:

```kotlin
// In AlgorithmEvent sealed interface:
data class Compare(
    val index1: Int,
    val index2: Int,
    val description: String = ""   // NEW
) : AlgorithmEvent

data class Swap(
    val index1: Int,
    val index2: Int,
    val description: String = ""   // NEW
) : AlgorithmEvent

// Similarly: Pivot, Overwrite, Select, Probe, Found, RangeCheck
```

**Example descriptions per algorithm**:

| Event | BubbleSort | QuickSort | BinarySearch |
|-------|-----------|-----------|-------------|
| Compare | "Comparing arr[2]=7 with arr[3]=2" | "Comparing arr[4]=3 with pivot=5" | "Checking mid element arr[5]=8" |
| Swap | "Since 7 > 2, swap positions 2 and 3" | "Moving 3 to left of pivot" | N/A |
| Pivot | N/A | "Chose pivot: arr[6]=5" | N/A |
| Probe | N/A | N/A | "Probing index 5: value=8, target=3 → go left" |
| Found | N/A | N/A | "Found target 3 at index 2!" |

**Effort**: Medium. 14 algorithm files to update, each ~10-20 description strings.

### A3. Pseudocode Panel Component

**New file**: `algo-ui-shared/.../ui/components/PseudocodePanel.kt`

Features:
- Monospace font, line numbers, syntax-colored keywords
- **Active line highlighting** — bright background on the line matching current operation
- Smooth highlight transition (animate background color)
- Map `HighlightReason` → pseudocode line:
  - `Compare` → highlight the comparison line
  - `Swap` → highlight the swap line
  - `Pivot` → highlight the pivot selection line
  - etc.

**Mapping mechanism**: Each `AlgorithmEvent` gets optional `pseudocodeLine: Int?` field. UI reads it to highlight the matching line.

**Effort**: Medium. New Compose component ~150 lines.

### A4. Step Explanation Component

**New file**: `algo-ui-shared/.../ui/components/StepExplanationPanel.kt`

Features:
- Displays `event.description` as human-readable text
- Styled with monospace for values, bold for operations
- Auto-scrolls as events progress
- Shows event counter "Step 5 of 28"
- Fades between descriptions (cross-fade animation)

**Effort**: Medium. New Compose component ~100 lines.

### A5. Complexity Info Panel

**New file**: `algo-ui-shared/.../ui/components/ComplexityPanel.kt`

Features:
- Best/Average/Worst time complexity in a table
- Space complexity
- Stability badge (green ✓ / red ✗)
- Difficulty stars (★☆☆ / ★★☆ / ★★★)
- Tags as small chips ("divide-and-conquer", "comparison-based")

**Effort**: Low. New Compose component ~80 lines.

### A6. Color Legend

**New file**: `algo-ui-shared/.../ui/components/LegendPanel.kt`

Compact, always-visible legend:
```
🟡 Comparing  🔴 Swapping  🟣 Pivot  🔵 Selecting  🟠 Overwriting  🟢 Sorted
```

Horizontal row of colored dots + labels. Placed between visualization and playback controls.

**Effort**: Low. ~40 lines.

---

## Phase B: "Explore" — Better Interaction

### B1. Input Presets

**File**: `InputConfigPanel.kt`

Quick-select buttons for common input patterns:
- **Random** (existing)
- **Nearly Sorted** (1-2 elements out of place)
- **Reversed** (descending order)
- **All Equal** (every element same value)
- **Many Duplicates** (few unique values, repeated)
- **Custom** (existing text input)

Each preset generates appropriate `List<Int>`.

**Effort**: Low. ~60 lines in existing component.

### B2. Progress Scrubber (Seek Bar)

**File**: `PlaybackControls.kt` + `AppViewModel.kt`

Slider that maps to event index. Drag to jump to any event.
- Shows current position / total
- Snap-to-event (discrete steps, not continuous)
- `seekTo(eventIndex: Int)` on ViewModel

**Effort**: Medium. Requires `SnapshotReconstructor` to support random access (may already — verify).

### B3. Centralized Color Theme

**New file**: `algo-ui-shared/.../ui/theme/VizColors.kt`

Single object defining ALL colors:
```kotlin
object VizColors {
    // Backgrounds
    val surfaceDark = Color(0xFF1E1E2E)
    val canvasDark = Color(0xFF1A1A2E)
    
    // Semantic highlight colors (shared across sort + search)
    val comparing = Color(0xFFFFD93D)
    val swapping = Color(0xFFFF6B6B)
    val pivoting = Color(0xFFA855F7)
    val selecting = Color(0xFF60A5FA)
    val overwriting = Color(0xFFF97316)
    val sorted = Color(0xFF22C55E)
    val found = Color(0xFF22C55E)
    val probing = Color(0xFFFFD93D)
    val rangeHighlight = Color(0xFF818CF8)
    val defaultElement = Color(0xFF4ECDC4)
    
    // Text
    val textPrimary = Color(0xFFF8F8F2)
    val textMuted = Color(0xFF8888A8)
    val accent = Color(0xFF7C7CF8)
}
```

Remove all private color constants from `SortVisualization`, `SearchVisualization`, `StatsPanel`, `NavigationPanel`.

**Effort**: Low. Extract + replace.

### B4. Sidebar Hover Effects + Search Filter

**File**: `NavigationPanel.kt`

- Apply existing `SidebarHoverBg` color on pointer hover
- Add text filter input at top of sidebar
- Filter algorithms by name as user types

**Effort**: Low. ~40 lines.

---

## Phase C: "Delight" — Polish & Wow

### C1. Completion Celebration

**File**: `SortVisualization.kt`

When `PlaybackState.Complete`:
- Sweep rainbow gradient across all bars left→right
- Bars bounce with spring animation
- Duration: ~1.5 seconds, then settle to green

**Effort**: Low. ~50 lines.

### C2. Index Labels on Bars/Cells

**Files**: `SortVisualization.kt`, `SearchVisualization.kt`

- Show index number below each bar (when ≤25 elements)
- Show index number inside each search cell

**Effort**: Low. ~20 lines each file.

### C3. Keyboard Shortcut Hints in UI

**File**: `PlaybackControls.kt`

Small muted text below each button:
- Space → Play/Pause
- → → Step Forward
- ← → Step Back
- R → Reset

**Effort**: Low. ~20 lines.

### C4. Responsive Layout

**File**: `AppContent.kt`

- `BoxWithConstraints` to detect width
- < 900dp: sidebar becomes hamburger-menu drawer, right panel becomes bottom sheet
- ≥ 900dp: three-column layout as designed

**Effort**: Medium. Layout restructuring ~150 lines.

---

## AlgorithmInfo Data (All 14 Algorithms)

### Sort Algorithms

| Algorithm | Difficulty | Best | Avg | Worst | Space | Stable | Tags |
|-----------|-----------|------|-----|-------|-------|--------|------|
| Bubble Sort | Beginner | O(n) | O(n²) | O(n²) | O(1) | ✅ | comparison-based, in-place |
| Selection Sort | Beginner | O(n²) | O(n²) | O(n²) | O(1) | ❌ | comparison-based, in-place |
| Insertion Sort | Beginner | O(n) | O(n²) | O(n²) | O(1) | ✅ | comparison-based, in-place, adaptive |
| Quick Sort | Intermediate | O(n log n) | O(n log n) | O(n²) | O(log n) | ❌ | divide-and-conquer, comparison-based, in-place |
| Merge Sort | Intermediate | O(n log n) | O(n log n) | O(n log n) | O(n) | ✅ | divide-and-conquer, comparison-based, stable |
| Heap Sort | Intermediate | O(n log n) | O(n log n) | O(n log n) | O(1) | ❌ | comparison-based, in-place, heap |
| Shell Sort | Intermediate | O(n log n) | O(n^1.25) | O(n²) | O(1) | ❌ | comparison-based, in-place, gap-sequence |
| Counting Sort | Intermediate | O(n+k) | O(n+k) | O(n+k) | O(k) | ✅ | non-comparison, integer-only |
| Cocktail Sort | Beginner | O(n) | O(n²) | O(n²) | O(1) | ✅ | comparison-based, in-place, bidirectional |
| Cycle Sort | Advanced | O(n²) | O(n²) | O(n²) | O(1) | ❌ | comparison-based, in-place, minimal-writes |
| Radix Sort | Intermediate | O(d·n) | O(d·n) | O(d·n) | O(n+d) | ✅ | non-comparison, integer-only, LSD |

### Search Algorithms

| Algorithm | Difficulty | Best | Avg | Worst | Space | Tags |
|-----------|-----------|------|-----|-------|-------|------|
| Linear Search | Beginner | O(1) | O(n) | O(n) | O(1) | sequential, unsorted-ok |
| Binary Search (Iterative) | Beginner | O(1) | O(log n) | O(log n) | O(1) | divide-and-conquer, requires-sorted |
| Binary Search (Recursive) | Beginner | O(1) | O(log n) | O(log n) | O(log n) | divide-and-conquer, requires-sorted, recursive |

---

## Event Description Examples (Per Algorithm)

### BubbleSort
```
Compare: "Comparing arr[{i}]={v1} with arr[{i+1}]={v2}"
Swap:    "{v1} > {v2}, so swap positions {i} and {i+1}"
Complete: "Array is sorted! No swaps needed in this pass."
```

### QuickSort
```
Pivot:   "Selecting pivot: arr[{hi}]={pivotVal}"
Compare: "Comparing arr[{j}]={v} with pivot={pivotVal}"
Swap:    "Swapping arr[{i}]={v1} and arr[{j}]={v2}"
Overwrite: "Placing pivot at its correct position {p}"
```

### BinarySearch
```
RangeCheck: "Search range: [{lo}..{hi}], mid={mid}"
Probe:   "Checking arr[{mid}]={val}. {val} {</>} {target} → go {left/right}"
Found:   "Found target {target} at index {mid}!"
NotFound: "Target {target} not found in the array."
```

---

## Files Modified/Created

### New Files (~8)
| File | Purpose |
|------|---------|
| `algo-ui-shared/.../ui/theme/VizColors.kt` | Centralized color palette |
| `algo-ui-shared/.../ui/components/PseudocodePanel.kt` | Pseudocode with line highlighting |
| `algo-ui-shared/.../ui/components/StepExplanationPanel.kt` | Human-readable step narrative |
| `algo-ui-shared/.../ui/components/ComplexityPanel.kt` | Complexity table + stability + difficulty |
| `algo-ui-shared/.../ui/components/LegendPanel.kt` | Color legend |
| `algo-ui-shared/.../ui/components/InfoPanel.kt` | Right column container (pseudocode + explanation + complexity) |
| `algo-ui-shared/.../model/PseudocodeLine.kt` | Pseudocode data model |
| `algo-ui-shared/.../model/Complexity.kt` | Complexity data model |

### Modified Files (~20)
| File | Changes |
|------|---------|
| `algo-shared/.../AlgorithmEvent.kt` | Add `description: String` + `pseudocodeLine: Int?` to events |
| `algo-shared/.../HighlightReason.kt` | (verify — may need no changes) |
| `algo-ui-shared/.../model/AlgorithmInfo.kt` | Add difficulty, complexity, stability, tags, pseudocode |
| `algo-ui-shared/.../model/AlgorithmRegistry.kt` | Populate rich metadata for all 14 algorithms |
| `algo-ui-shared/.../model/AlgorithmCategory.kt` | (no change needed — SORTING/SEARCHING sufficient) |
| `algo-ui-shared/.../ui/AppContent.kt` | Three-column layout |
| `algo-ui-shared/.../ui/AppViewModel.kt` | Expose current step description, pseudocode highlight |
| `algo-ui-shared/.../ui/components/SortVisualization.kt` | Use VizColors, add index labels, completion celebration |
| `algo-ui-shared/.../ui/components/SearchVisualization.kt` | Use VizColors, add index labels |
| `algo-ui-shared/.../ui/components/StatsPanel.kt` | Use VizColors, simplify (complexity moves to ComplexityPanel) |
| `algo-ui-shared/.../ui/components/NavigationPanel.kt` | Use VizColors, add hover effects, search filter |
| `algo-ui-shared/.../ui/components/PlaybackControls.kt` | Add scrubber, keyboard hints, use VizColors |
| `algo-ui-shared/.../ui/components/InputConfigPanel.kt` | Add preset buttons, use VizColors |
| `algo-core/.../sorts/BubbleSort.kt` | Add descriptions to events |
| `algo-core/.../sorts/SelectionSort.kt` | Add descriptions to events |
| `algo-core/.../sorts/InsertionSort.kt` | Add descriptions to events |
| `algo-core/.../sorts/QuickSort.kt` | Add descriptions to events |
| `algo-core/.../sorts/MergeSort.kt` | Add descriptions to events |
| `algo-core/.../sorts/HeapSort.kt` | Add descriptions to events |
| + 6 more sort/search files | Add descriptions to events |

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Event API change breaks existing code | Medium | Default params `description=""` → backward compatible |
| Three-column layout too cramped on <1200px | Medium | Phase C responsive layout handles this |
| Pseudocode highlighting sync off | Low | `pseudocodeLine` on events is explicit — no guesswork |
| Too much info overwhelms students | Low | Info panel collapsible; clean visual hierarchy |
| 14 algorithms × descriptions = lots of code | Medium | Helper function `describeCompare(i, v1, j, v2)` to DRY it up |

---

## Success Criteria

- [ ] Every algorithm step shows a human-readable explanation
- [ ] Pseudocode panel highlights the current line in sync with visualization
- [ ] Color legend visible without scrolling
- [ ] Complexity info (best/avg/worst/space/stability) shown for every algorithm
- [ ] All colors centralized in single `VizColors` object
- [ ] Input presets work (random, nearly-sorted, reversed, all-equal, duplicates)
- [ ] Progress scrubber allows seeking to any event
- [ ] Completion celebration animation fires when sort completes
- [ ] Index labels visible on bars and cells
- [ ] All 147+ existing tests still pass

---

## Recommended Implementation Order

```
Phase A (must-have, ~3-4 sessions):
  A3. Centralized colors (VizColors)          ← do first, everything depends on it
  A1. Rich AlgorithmInfo model                ← data foundation
  A2. Event descriptions (all 14 algorithms)  ← biggest effort item
  A3. Pseudocode panel                        ← new component
  A4. Step explanation panel                  ← new component
  A5. Complexity info panel                   ← new component
  A6. Color legend                            ← quick win
  → Three-column layout integration           ← wire everything together

Phase B (should-have, ~2 sessions):
  B1. Input presets
  B2. Progress scrubber
  B4. Sidebar hover + search filter

Phase C (nice-to-have, ~1-2 sessions):
  C1. Completion celebration
  C2. Index labels
  C3. Keyboard hints
  C4. Responsive layout
```

---

## Unresolved Questions

1. Should pseudocode use Kotlin syntax or generic pseudocode (if/for/swap)? → *Recommend: generic pseudocode — more universal for students*
2. Should event descriptions include array values? (requires accessing array state) → *Yes — most educational. Emitter has access to array.*
3. Max pseudocode line length for 280dp panel? → *Test empirically, ~40 chars should fit*
