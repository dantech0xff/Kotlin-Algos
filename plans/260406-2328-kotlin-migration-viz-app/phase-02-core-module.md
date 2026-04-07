# Phase 02: Core Module — Shared Interfaces, Events, Base Infrastructure

**Date:** 2026-04-07
**Priority:** P1
**Status:** [ ] Pending

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: Phase 01 (project setup)
- Research: [researcher-01-compose-viz-architecture.md](research/researcher-01-compose-viz-architecture.md), [researcher-02-kotlin-migration-patterns.md](research/researcher-02-kotlin-migration-patterns.md)
- Existing Java interfaces: `SortAlgorithm.java`, `SearchAlgorithm.java`, `SortUtils.java`

## Overview

Define the Kotlin API surface that all migrated algorithms implement. Create the visualization event system (sealed class hierarchy), the `VisualizableAlgorithm` interface, and base test infrastructure. This is the contract that Phase 04/05 implementations and Phase 03 engine consume.

## Key Insights

- **[VALIDATED]** Use generic `T : Comparable<T>` from start — not Int-only. No technical debt.
- Java `SortAlgorithm` uses `T[]` arrays. Kotlin idiomatic approach prefers `List<T>`. We expose `List<T>` in the Kotlin API and internally convert as needed.
- Sealed class for `AlgorithmEvent` gives exhaustive `when` matching — critical for visualization correctness.
- `MutableSharedFlow<AlgorithmEvent>` is the emitter passed to algorithms. Algorithms emit events; engine collects them.
- Existing `SortUtils` helper methods (`swap`, `less`, `greater`) become Kotlin extension functions on `MutableList<T>`.
- **[VALIDATED]** Search algorithms use `SearchInput(array, key)` data class instead of last-element-as-key convention.

## Requirements

### Functional
- `SortAlgorithm` Kotlin interface: `fun <T : Comparable<T>> sort(list: List<T>): List<T>`
- `SearchAlgorithm` Kotlin interface: `fun <T : Comparable<T>> find(array: List<T>, key: T): Int`
- `VisualizableAlgorithm` interface with `suspend fun <T : Comparable<T>> execute(input: List<T>, emitter: MutableSharedFlow<AlgorithmEvent>)`
- `SearchInput<T>` data class: `data class SearchInput<T : Comparable<T>>(val array: List<T>, val key: T)`
- `SearchVisualizableAlgorithm` interface with `suspend fun <T : Comparable<T>> execute(input: SearchInput<T>, emitter: MutableSharedFlow<AlgorithmEvent>)`
- `AlgorithmEvent` sealed class hierarchy: `Start`, `Compare`, `Swap`, `Select`, `Pivot`, `Overwrite`, `Complete`, `Found`, `Probe` (for search)
- `SortUtilsKt` extension functions: `MutableList<T>.swapAt(i, j)`, `Comparable<T>.isLessThan(other)`, `MutableList<T>.isSorted()`
- Base test class `SortAlgorithmTest` with common test cases (empty, single, duplicates, sorted, reverse, random)
- Base test class `SearchAlgorithmTest` with common test cases

### Non-Functional
- All interfaces in `algo-shared` module (zero algorithm dependencies)
- Extension functions in `algo-core` module
- Tests in `algo-core` test source set
- Kotest framework for all tests

## Architecture

### Module Dependency

```
algo-shared (interfaces + events) ← algo-core (implementations + utils + tests)
```

### Event Hierarchy

```
AlgorithmEvent (sealed interface)
├── Start(data: List<Int>)
├── Compare(indices: Pair<Int, Int>)
├── Swap(indices: Pair<Int, Int>)
├── Select(index: Int)
├── Deselect(index: Int)
├── Pivot(index: Int)
├── Overwrite(index: Int, newValue: Int)
├── Complete(result: List<Int>)
├── Probe(index: Int)                    // search: examining this index
├── Found(index: Int)
├── NotFound
└── RangeCheck(low: Int, high: Int)      // binary search: narrowing range
```

### Data Flow

```
Algorithm.execute(input, emitter)
    ↓ emit(AlgorithmEvent.*)
MutableSharedFlow<AlgorithmEvent>
    ↓ collect
AlgorithmPlayer (Phase 03)
    ↓ StateFlow<PlaybackState>
Compose UI (Phase 06)
```

## Related Code Files

### Create (algo-shared)
- `algo-shared/src/commonMain/kotlin/com/thealgorithms/shared/SortAlgorithm.kt`
- `algo-shared/src/commonMain/kotlin/com/thealgorithms/shared/SearchAlgorithm.kt`
- `algo-shared/src/commonMain/kotlin/com/thealgorithms/shared/VisualizableAlgorithm.kt`
- `algo-shared/src/commonMain/kotlin/com/thealgorithms/shared/SearchVisualizableAlgorithm.kt`
- `algo-shared/src/commonMain/kotlin/com/thealgorithms/shared/SearchInput.kt`
- `algo-shared/src/commonMain/kotlin/com/thealgorithms/shared/AlgorithmEvent.kt`
- `algo-shared/src/commonMain/kotlin/com/thealgorithms/shared/PlaybackState.kt`

### Create (algo-core)
- `algo-core/src/commonMain/kotlin/com/thealgorithms/core/utils/SortUtils.kt`
- `algo-core/src/commonMain/kotlin/com/thealgorithms/core/utils/SearchUtils.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/SortAlgorithmTestBase.kt`
- `algo-core/src/commonTest/kotlin/com/thealgorithms/core/SearchAlgorithmTestBase.kt`

### Modify
- `algo-core/build.gradle.kts` — add Kotest dependencies
- `algo-shared/build.gradle.kts` — add coroutines dependency (for SharedFlow type)

### Delete
- None

## Implementation Steps

1. In `algo-shared`, create `AlgorithmEvent.kt` — sealed interface with all event subtypes
2. In `algo-shared`, create `PlaybackState.kt` — sealed interface: `Stopped`, `Playing`, `Paused`, `StepForward`, `StepBack`, `Complete`
3. In `algo-shared`, create `SortAlgorithm.kt` interface
4. In `algo-shared`, create `SearchAlgorithm.kt` interface
5. In `algo-shared`, create `VisualizableAlgorithm.kt` interface
6. In `algo-core`, create `SortUtils.kt` with extension functions
7. In `algo-core`, create `SearchUtils.kt` with extension functions
8. Add Kotest dependencies to `algo-core/build.gradle.kts`
9. Create `SortAlgorithmTestBase.kt` — abstract spec with parameterized test cases
10. Create `SearchAlgorithmTestBase.kt` — abstract spec with parameterized test cases
11. Write smoke tests for `AlgorithmEvent` sealed class exhaustive matching
12. Verify: `./gradlew :algo-core:check` passes

## Todo List

- [ ] Create `AlgorithmEvent.kt` sealed interface
- [ ] Create `PlaybackState.kt` sealed interface
- [ ] Create `SortAlgorithm.kt` interface
- [ ] Create `SearchAlgorithm.kt` interface
- [ ] Create `VisualizableAlgorithm.kt` interface
- [ ] Create `SortUtils.kt` extension functions
- [ ] Create `SearchUtils.kt` extension functions
- [ ] Add Kotest to `algo-core/build.gradle.kts`
- [ ] Create `SortAlgorithmTestBase.kt`
- [ ] Create `SearchAlgorithmTestBase.kt`
- [ ] Create `AlgorithmEvent` smoke tests
- [ ] Verify `./gradlew :algo-core:check` passes

## Success Criteria

- [ ] All interfaces compile and are accessible from dependent modules
- [ ] `AlgorithmEvent` has exhaustive `when` matching with no `else` branch needed
- [ ] `SortUtils` extension functions work on `MutableList<Int>` and `MutableList<String>`
- [ ] `SortAlgorithmTestBase` runs 10+ parameterized test cases
- [ ] `./gradlew :algo-core:check` passes

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| SharedFlow type requires coroutines dependency in shared module | High | Low | Add kotlinx-coroutines-core as `api` dependency in algo-shared |
| Generic type erasure limits event type safety | Low | Low | Use `Int` for MVP; parameterize later if needed |
| Sealed interface across modules requires same package | Low | Medium | Keep all events in `com.thealgorithms.shared` |

## Security Considerations

- None (pure interface definitions, no I/O)

## Next Steps

- Phase 03 (Viz Engine) consumes `AlgorithmEvent`, `VisualizableAlgorithm`, `PlaybackState`
- Phase 04 (Sorting) implements `VisualizableAlgorithm` for 5 sort algorithms
- Phase 05 (Search) implements `VisualizableAlgorithm` for 3 search algorithms
