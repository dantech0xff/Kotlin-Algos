# Code Review: Educational Excellence — 5 Phases

**Date:** 2026-04-08
**Reviewer:** Code Reviewer (Automated)
**Scope:** All changes across Phases 1–5 of Educational Excellence plan

---

## Scope

- **Files reviewed:** 28 new + modified files
- **Lines analyzed:** ~2800 LOC
- **Focus:** Recent session changes implementing educational features
- **Updated plans:** `plans/260408-educational-excellence/plan.md`

---

## Scores

| Area | Score | Notes |
|------|-------|-------|
| Correctness | 7/10 | Swap description ordering bug in 4 files; CocktailSort pseudocode gaps |
| Consistency | 8/10 | A few hardcoded colors remain; Select/Deselect lack descriptions |
| Performance | 9/10 | No concerns; lazy columns, efficient snapshot reconstruction |
| Educational Value | 8/10 | Good descriptions, but swap values are misleading |
| Maintainability | 9/10 | Clean separation, centralized colors, well-structured models |

**Overall: FIX FIRST** — 1 critical bug, 3 major issues to address before shipping.

---

## CRITICAL Issues

### C1. Swap descriptions show POST-swap values (4 files)

**Files:** `BubbleSort.kt`, `CocktailSort.kt`, `QuickSort.kt`, `SelectionSort.kt`

In these files, `arr.swapAt()` is called **before** `DescriptionUtils.swap()`. By the time the description is generated, the array has already been mutated, so the values shown are **reversed**. For a student seeing "Swapping arr[0]=3 and arr[1]=5", the actual pre-swap values were arr[0]=5, arr[1]=3.

The plan's own risk assessment warned: *"Descriptions reference stale array values → Emit AFTER mutation, not before"* — but this was implemented incorrectly for 4 of 6 swap-based sorts.

**HeapSort** and **ShellSort** do this correctly (emit before swap).

```kotlin
// BUG (BubbleSort.kt line 42-45):
arr.swapAt(j, j + 1)  // ← mutates array FIRST
emitter.emit(AlgorithmEvent.Swap(
    indices = j to j + 1,
    description = DescriptionUtils.swap(j, j + 1, arr), // ← reads post-swap values
    ...
))

// FIX: capture description BEFORE swapAt
emitter.emit(AlgorithmEvent.Swap(
    indices = j to j + 1,
    description = DescriptionUtils.swap(j, j + 1, arr), // ← reads pre-swap values
    ...
))
arr.swapAt(j, j + 1)
```

**Impact:** Students see wrong values in swap descriptions — educationally harmful.

---

## MAJOR Issues

### M1. CocktailSort pseudocode line numbers don't match emitted lines

**File:** `AlgorithmRegistry.kt` (CocktailSort entry)

The pseudocode defines lines 1, 2, 5, 9 but the visualizer emits `pseudocodeLine = 6` (forward swap) and `pseudocodeLine = 10` (backward swap). Lines 6 and 10 don't exist in the pseudocode, so swap events will never highlight any pseudocode line.

```kotlin
// Current pseudocode (gaps at 3,4,6,7,8,10):
PseudocodeLine("function cocktailSort(arr):", 0, 1),
PseudocodeLine("while swapped:", 1, 2),
PseudocodeLine("forward: if arr[i]>arr[i+1]: swap", 2, 5),
PseudocodeLine("backward: if arr[i]>arr[i+1]: swap", 2, 9),

// Fix: add lines for swap actions:
PseudocodeLine("function cocktailSort(arr):", 0, 1),
PseudocodeLine("while swapped:", 1, 2),
PseudocodeLine("forward pass:", 1, 3),
PseudocodeLine("if arr[i] > arr[i+1]:", 2, 4),
PseudocodeLine("swap(arr[i], arr[i+1])", 3, 5),
PseudocodeLine("end--", 1, 6),
PseudocodeLine("backward pass:", 1, 7),
PseudocodeLine("if arr[i] > arr[i+1]:", 2, 8),
PseudocodeLine("swap(arr[i], arr[i+1])", 3, 9),
PseudocodeLine("start++", 1, 10),
```

### M2. Hardcoded `Color.Gray` in StatsPanel

**File:** `StatsPanel.kt` line 110

```kotlin
else -> Triple("Stopped", Color.Gray.copy(alpha = 0.2f), Color.Gray)
```

Should use `VizColors.textMuted` or a new `VizColors.stateStopped` entry.

### M3. Hardcoded `Color.White` / `Color.Black` in SortVisualization

**File:** `SortVisualization.kt` lines 120, 144, 185

```kotlin
color = Color.White.copy(alpha = 0.08f)  // grid lines (line 120)
color = Color.White.copy(alpha = 0.85f)  // labels (line 144)
color = Color.Black.copy(alpha = 0.2f)   // shadow (line 185)
```

These could use `VizColors.textPrimary` (for labels), `VizColors.gridDark` (for grid), `VizColors.divider` (for shadow). While `Color.White`/`Black` with alpha are reasonable for visual effects, they break the "single source of truth" principle the plan established.

### M4. `parseArray` filters out non-positive values

**File:** `InputConfigPanel.kt` line 132

```kotlin
.filter { it > 0 }
```

This prevents students from entering 0 or negative values. CountingSort specifically handles negatives, and real-world arrays can contain 0. Should be `.filter { true }` or just removed.

---

## MINOR Issues

### m1. `DescriptionUtils` doesn't match plan's design

The plan specified richer descriptions like `"Since 5 > 3, swap positions 0 and 1"` and helpers for `noSwap`, `probe`, `found`, `notFound`, `rangeCheck`. The actual implementation is simpler: `"Swapping arr[0]=5 and arr[3]=2"`. Functionally correct but less educational.

### m2. `AlgorithmEvent.Select`/`Deselect` lack description/pseudocodeLine

`Select` and `Deselect` events carry no description or pseudocode line reference. When a SelectionSort selects a minimum candidate, the step explanation panel shows nothing about it. Inconsistent with all other event types.

### m3. `highlightedIndices` legacy accessor in AlgorithmSnapshot

```kotlin
val highlightedIndices: Set<Int> get() = highlights.keys
```

This migration shim should be annotated `@Deprecated` with a removal timeline.

### m4. `AlgorithmInfo.visualizer: Any` with unsafe casts

`AlgorithmRegistry.visualizerAsSort()` and `visualizerAsSearch()` use `as` casts. This works but is fragile. A generic type parameter or sealed hierarchy would be safer.

### m5. Inline descriptions in MergeSort and CycleSort

- `MergeSort.kt` line 86: hardcoded `"Comparing left[${left+i}]=... with right[...]` instead of `DescriptionUtils.compare()`
- `CycleSort.kt` line 72: hardcoded `"Comparing item=$item with arr[$i]=..."` instead of `DescriptionUtils.compare()`

These actually provide MORE context than DescriptionUtils (left/right partition info, item tracking), so this is arguably better. But it's inconsistent.

### m6. Search descriptions use inline strings instead of DescriptionUtils

LinearSearch, IterativeBinarySearch, RecursiveBinarySearch all use inline description strings. Again, arguably more contextual, but inconsistent with sort algorithms that use DescriptionUtils.

---

## Positive Observations

1. **VizColors centralization** — Excellent single-source-of-truth palette covering surfaces, semantics, text, sidebar, stats, pseudocode, and misc. 100% of new code uses it.

2. **Backward-compatible event API** — Default params (`description = ""`, `pseudocodeLine = null`) mean zero breakage. All existing tests pass.

3. **SnapshotReconstructor** — Clean propagation of description + pseudocodeLine from events to snapshots. Every event type handled correctly.

4. **InfoPanel composition** — Clean composable hierarchy: `InfoPanel → PseudocodePanel + StepExplanationPanel + ComplexityPanel + LegendPanel`. Each is focused and reusable.

5. **PseudocodePanel** — Efficient `LazyColumn` with keyed items, monospace font, active line highlighting. Clean implementation.

6. **AlgorithmRegistry metadata** — All 14 algorithms have complete metadata: complexity, stability, difficulty, tags, pseudocode. Well-organized.

7. **Three-column layout** — Clean separation: Nav(200dp) | Viz(flex) | Info(280dp conditional). Good use of `VerticalDivider`.

8. **Test coverage** — Search visualization tests updated to verify descriptions and pseudocode lines. All passing.

9. **Build verification** — `./gradlew algo-ui-shared:build algo-viz-engine:build algo-core:build` → BUILD SUCCESSFUL.

---

## Recommended Actions

1. **[CRITICAL]** Fix swap description ordering in BubbleSort, CocktailSort, QuickSort, SelectionSort — move `swapAt()` AFTER emit
2. **[MAJOR]** Fix CocktailSort pseudocode to include lines 6 and 10 (or renumber emitted lines)
3. **[MAJOR]** Replace `Color.Gray` in StatsPanel with VizColors reference
4. **[MAJOR]** Remove `.filter { it > 0 }` from `parseArray()` in InputConfigPanel
5. **[MINOR]** Consider enriching `DescriptionUtils.swap()` to explain WHY (e.g., "Since 5 > 3, swapping")
6. **[MINOR]** Add description/pseudocodeLine to Select/Deselect events

---

## Metrics

- Build: ✅ SUCCESSFUL
- Test Suite: ✅ All passing
- Hardcoded Colors: 3 remaining (`Color.Gray`, `Color.White`×2, `Color.Black`×1)
- VizColors Usage: ~95% centralized
- Pseudocode Coverage: 14/14 algorithms
- Description Coverage: 14/14 algorithms (but 4 have wrong swap values)
- Event Type Coverage: 7/9 event types have description (missing Select/Deselect)

---

## Unresolved Questions

1. Should `DescriptionUtils.swap()` be enriched with the "since X > Y" explanation as the plan originally specified?
2. Should Select/Deselect events get description fields, or is the current silent behavior acceptable?
3. Should the `parseArray` filter be removed entirely or just changed to allow 0 and negatives?
4. Is the `Color.White`/`Color.Black` with alpha in SortVisualization effects acceptable as-is, or should these be centralized?

---

*Report generated: 2026-04-08*
