# Phase 05: Search Algorithms MVP — Migrate 3 Search Algorithms with Visualization

**Date:** 2026-04-07
**Priority:** P1
**Status:** [ ] Pending

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: Phase 02 (core interfaces), Phase 03 (viz engine)
- Research: [scout-01-algorithm-structure.md](scout/scout-01-algorithm-structure.md)
- Java source: `src/main/java/com/thealgorithms/searches/`

## Overview

Migrate 3 search algorithms from Java to idiomatic Kotlin with `VisualizableAlgorithm` support. Search algorithms use different events (`Probe`, `Found`, `NotFound`, `RangeCheck`) to visualize the search process.

### Algorithms
| # | Algorithm | Complexity | Key Events |
|---|-----------|-----------|------------|
| 1 | LinearSearch | O(n) | Probe, Found, NotFound |
| 2 | BinarySearch (Iterative) | O(log n) | Probe, RangeCheck, Found, NotFound |
| 3 | BinarySearch (Recursive) | O(log n) | Probe, RangeCheck, Found, NotFound |

## Key Insights

- **[VALIDATED]** Use `SearchInput(array, key)` data class — type-safe, no hacky conventions.
- LinearSearch is trivial — iterate + emit `Probe(i)` per element.
- BinarySearch (iterative + recursive) emit `RangeCheck(low, high)` to visualize narrowing search space.
- Both BinarySearch variants already exist as separate Java files: `IterativeBinarySearch.java` and `RecursiveBinarySearch.java`.
- Search visualization is simpler than sort — no mutations, just probing/comparing.
- **[VALIDATED]** Generic `T : Comparable<T>` from start.

## Requirements

### Functional
Each algorithm must:
- Implement `SearchVisualizableAlgorithm` interface (uses `SearchInput<T>` instead of raw `List<Int>`)
- Implement `SearchAlgorithm` interface (pure search, no events)
- Emit `AlgorithmEvent.Start(array)` at beginning
- Emit `AlgorithmEvent.Probe(index)` when examining an element
- Emit `AlgorithmEvent.RangeCheck(low, high)` for binary search narrowing
- Emit `AlgorithmEvent.Found(index)` or `AlgorithmEvent.NotFound` at end
- Handle edge cases: empty array, single element, key not present, key at boundaries, duplicates
- Support generic `T : Comparable<T>` for both correctness and visualization

### Non-Functional
- Each file < 80 lines
- Kotest tests following `SearchAlgorithmTestBase` from Phase 02
- Event sequence tests verify correct probing order

## Architecture

### File Layout

```
algo-core/src/commonMain/kotlin/com/thealgorithms/core/searches/
├── LinearSearch.kt
├── IterativeBinarySearch.kt
└── RecursiveBinarySearch.kt
```

### Search Visualization Pattern

```kotlin
class LinearSearchVisualizer : SearchVisualizableAlgorithm {
    override suspend fun <T : Comparable<T>> execute(
        input: SearchInput<T>,
        emitter: MutableSharedFlow<AlgorithmEvent>
    ) {
        emitter.emit(AlgorithmEvent.Start(input.array))

        for (i in input.array.indices) {
            emitter.emit(AlgorithmEvent.Probe(i))
            if (input.array[i] == input.key) {
                emitter.emit(AlgorithmEvent.Found(i))
                return
            }
        }
        emitter.emit(AlgorithmEvent.NotFound)
    }
}
```

**[VALIDATED]** `SearchInput(array, key)` data class provides type-safe, clear separation of search target from data.

## Related Code Files

### Create (algo-core)
- `algo-core/src/commonMain/kotlin/com/thealgorithms/core/searches/LinearSearch.kt`
- `algo-core/src/commonMain/kotlin/com/thealgorithms/core/searches/IterativeBinarySearch.kt`
- `algo-core/src/commonMain/kotlin/com/thealgorithms/core/searches/RecursiveBinarySearch.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/searches/LinearSearchTest.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/searches/IterativeBinarySearchTest.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/searches/RecursiveBinarySearchTest.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/searches/SearchVisualizationTest.kt`

### Java Source (read-only reference)
- `src/main/java/com/thealgorithms/searches/LinearSearch.java`
- `src/main/java/com/thealgorithms/searches/IterativeBinarySearch.java`
- `src/main/java/com/thealgorithms/searches/RecursiveBinarySearch.java`

## Implementation Steps

1. Read Java `LinearSearch.java` → convert to Kotlin → add `SearchAlgorithm` + `VisualizableAlgorithm` impls
2. Write `LinearSearchTest` — correctness + edge cases
3. Read Java `IterativeBinarySearch.java` → convert → add visualizations with `Probe` + `RangeCheck`
4. Write `IterativeBinarySearchTest`
5. Read Java `RecursiveBinarySearch.java` → convert → add visualizations (pass `emitter` through recursion)
6. Write `RecursiveBinarySearchTest`
7. Write `SearchVisualizationTest` — verify event sequences for known inputs
8. Verify: `./gradlew :algo-core:check` passes with all search tests

## Todo List

- [ ] Migrate `LinearSearch` to Kotlin + visualization
- [ ] Write `LinearSearchTest`
- [ ] Migrate `IterativeBinarySearch` to Kotlin + visualization
- [ ] Write `IterativeBinarySearchTest`
- [ ] Migrate `RecursiveBinarySearch` to Kotlin + visualization
- [ ] Write `RecursiveBinarySearchTest`
- [ ] Write `SearchVisualizationTest` (event sequences)
- [ ] Verify `./gradlew :algo-core:check` passes

## Success Criteria

- [ ] All 3 algorithms find correctly: existing key, missing key, first/last element, duplicates
- [ ] LinearSearch on `[3,1,4,1,5]` searching for `4` emits: Start, Probe(0), Probe(1), Probe(2), Found(2)
- [ ] BinarySearch emits `RangeCheck` events showing narrowing `[low, high]`
- [ ] `NotFound` emitted when key absent
- [ ] All tests pass: `./gradlew :algo-core:check`

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Input convention (last=key) may confuse users | Medium | Low | Document clearly; UI shows key separately from array |
| Recursive BinarySearch must pass emitter through calls | Low | Low | Standard pattern — same as QuickSort in Phase 04 |

## Security Considerations

- None

## Next Steps

- Phase 06 (Desktop UI) renders search probe/range events
- Post-MVP: migrate remaining 34 search algorithms using same pattern
