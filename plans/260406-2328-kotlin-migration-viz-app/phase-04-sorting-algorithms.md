# Phase 04: Sorting Algorithms MVP — Migrate 5 Sort Algorithms with Visualization

**Date:** 2026-04-07
**Priority:** P1
**Status:** [ ] Pending

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: Phase 02 (core interfaces), Phase 03 (viz engine)
- Research: [researcher-02-kotlin-migration-patterns.md](research/researcher-02-kotlin-migration-patterns.md), [scout-01-algorithm-structure.md](scout/scout-01-algorithm-structure.md)
- Java source: `src/main/java/com/thealgorithms/sorts/`

## Overview

Migrate 5 sorting algorithms from Java to idiomatic Kotlin, implementing `VisualizableAlgorithm` for each. Algorithms emit fine-grained events (Compare, Swap, Select, Overwrite) at each step for real-time visualization.

### Algorithms
| # | Algorithm | Complexity | Key Events |
|---|-----------|-----------|------------|
| 1 | BubbleSort | O(n^2) | Compare, Swap |
| 2 | SelectionSort | O(n^2) | Compare, Swap, Select |
| 3 | InsertionSort | O(n^2) | Compare, Overwrite |
| 4 | QuickSort | O(n log n) avg | Compare, Swap, Pivot |
| 5 | MergeSort | O(n log n) | Compare, Overwrite |

## Key Insights

- Java algorithms use `T[]` arrays with `Comparable<T>`. Kotlin implementations use `MutableList<Int>` for MVP simplicity. Generic `T : Comparable<T>` deferred to post-MVP.
- Auto-convert Java to Kotlin first, then refactor to idiomatic Kotlin + add event emissions.
- `SortUtils.swap/less/greater` become extension functions from Phase 02.
- Event granularity: emit at every comparison and swap — gives best visualization fidelity.

## Requirements

### Functional
Each algorithm must:
- Implement `VisualizableAlgorithm` interface from `algo-shared`
- Emit `AlgorithmEvent.Start` at beginning
- Emit `AlgorithmEvent.Compare(i, j)` before each comparison
- Emit `AlgorithmEvent.Swap(i, j)` or `AlgorithmEvent.Overwrite(i, v)` after mutations
- Emit `AlgorithmEvent.Pivot(i)` for QuickSort partition selection
- Emit `AlgorithmEvent.Complete(sortedList)` at end
- Also implement `SortAlgorithm` interface (pure sort, no events) for testing core logic independently
- Handle edge cases: empty list, single element, already sorted, reverse sorted, duplicates

### Non-Functional
- Each algorithm file < 100 lines
- Kotest tests following `SortAlgorithmTestBase` pattern from Phase 02
- Separate test files for correctness (no events) and visualization (event sequences)

## Architecture

### Algorithm Implementation Pattern

```kotlin
class BubbleSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        val arr = input.toMutableList()
        emitter.emit(AlgorithmEvent.Start(input))
        // ... sorting logic with emitter.emit() calls ...
        emitter.emit(AlgorithmEvent.Complete(arr.toList()))
    }
}

class BubbleSort : SortAlgorithm {
    override fun <T : Comparable<T>> sort(list: List<T>): List<T> {
        // Pure sort logic, no visualization
    }
}
```

### File Layout

```
algo-core/src/commonMain/kotlin/com/thealgorithms/core/sorts/
├── BubbleSort.kt
├── SelectionSort.kt
├── InsertionSort.kt
├── QuickSort.kt
├── MergeSort.kt
└── VisualizableSorts.kt    // wrapper: visualizer delegates to sort + emits events
```

## Related Code Files

### Create (algo-core)
- `algo-core/src/commonMain/kotlin/com/thealgorithms/core/sorts/BubbleSort.kt`
- `algo-core/src/commonMain/kotlin/com/thealgorithms/core/sorts/SelectionSort.kt`
- `algo-core/src/commonMain/kotlin/com/thealgorithms/core/sorts/InsertionSort.kt`
- `algo-core/src/commonMain/kotlin/com/thealgorithms/core/sorts/QuickSort.kt`
- `algo-core/src/commonMain/kotlin/com/thealgorithms/core/sorts/MergeSort.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/sorts/BubbleSortTest.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/sorts/SelectionSortTest.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/sorts/InsertionSortTest.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/sorts/QuickSortTest.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/sorts/MergeSortTest.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/sorts/SortVisualizationTest.kt` (event sequence tests)

### Java Source (read-only reference)
- `src/main/java/com/thealgorithms/sorts/BubbleSort.java`
- `src/main/java/com/thealgorithms/sorts/SelectionSort.java`
- `src/main/java/com/thealgorithms/sorts/InsertionSort.java`
- `src/main/java/com/thealgorithms/sorts/QuickSort.java`
- `src/main/java/com/thealgorithms/sorts/MergeSort.java`

## Implementation Steps

1. Read Java `BubbleSort.java` → auto-convert to Kotlin → refactor to idiomatic (`for (i in 1 until size)`, extension functions)
2. Add `VisualizableAlgorithm` implementation: wrap sort logic, insert `emitter.emit()` calls at each comparison/swap
3. Write `BubbleSortTest` extending `SortAlgorithmTestBase` — correctness tests
4. Repeat steps 1-3 for `SelectionSort`
5. Repeat steps 1-3 for `InsertionSort`
6. Repeat steps 1-3 for `QuickSort` (partition + recursive sort, emit `Pivot` events)
7. Repeat steps 1-3 for `MergeSort` (auxiliary array, emit `Overwrite` events for merge step)
8. Write `SortVisualizationTest` — verify each algorithm emits correct event sequence for `[3, 1, 2]`
9. Verify: `./gradlew :algo-core:check` passes with all sort tests

## Todo List

- [ ] Migrate `BubbleSort` to Kotlin + visualization
- [ ] Write `BubbleSortTest`
- [ ] Migrate `SelectionSort` to Kotlin + visualization
- [ ] Write `SelectionSortTest`
- [ ] Migrate `InsertionSort` to Kotlin + visualization
- [ ] Write `InsertionSortTest`
- [ ] Migrate `QuickSort` to Kotlin + visualization
- [ ] Write `QuickSortTest`
- [ ] Migrate `MergeSort` to Kotlin + visualization
- [ ] Write `MergeSortTest`
- [ ] Write `SortVisualizationTest` (event sequences)
- [ ] Verify `./gradlew :algo-core:check` passes

## Success Criteria

- [ ] All 5 algorithms sort correctly on: empty, single, duplicates, sorted, reverse, random (100 elements)
- [ ] Each algorithm emits `Start` at beginning and `Complete` at end
- [ ] Event count matches expected operations for known inputs (e.g., BubbleSort on `[3,2,1]` emits 3 Compare + 3 Swap)
- [ ] QuickSort emits `Pivot` events at correct indices
- [ ] All tests pass: `./gradlew :algo-core:check`

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Recursive algorithms (QuickSort/MergeSort) emit events from nested calls | Medium | Medium | Pass `emitter` as parameter through recursive calls |
| Auto-conversion produces non-idiomatic Kotlin | High | Low | Manual review pass after conversion — well-defined patterns |
| MergeSort `Overwrite` events from auxiliary array | Medium | Low | Emit `Overwrite(index, newValue)` when writing back to main array |

## Security Considerations

- None (pure computation)

## Next Steps

- Phase 05 (Search MVP) follows same pattern for search algorithms
- Phase 06 (Desktop UI) renders these algorithms' events via the viz engine
