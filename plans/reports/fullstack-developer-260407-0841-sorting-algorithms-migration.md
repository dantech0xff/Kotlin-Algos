# Phase 04: Sorting Algorithms Migration - Implementation Report

## Executed Phase
- Phase: 04 - Sorting algorithms - 5 sorts with visualization
- Status: completed

## Files Modified

### Build config
- `algo-core/build.gradle.kts` - added coroutines-core + coroutines-test dependencies

### Source files (algo-core/src/main/kotlin/com/thealgorithms/core/sorts/)
- `BubbleSort.kt` (47 lines) - BubbleSort + BubbleSortVisualizer
- `SelectionSort.kt` (58 lines) - SelectionSort + SelectionSortVisualizer
- `InsertionSort.kt` (49 lines) - InsertionSort + InsertionSortVisualizer
- `QuickSort.kt` (86 lines) - QuickSort + QuickSortVisualizer
- `MergeSort.kt` (103 lines) - MergeSort + MergeSortVisualizer

### Test files (algo-core/src/test/kotlin/com/thealgorithms/core/sorts/)
- `BubbleSortTest.kt` (48 lines) - 7 sort correctness tests
- `SelectionSortTest.kt` (48 lines) - 7 sort correctness tests
- `InsertionSortTest.kt` (48 lines) - 7 sort correctness tests
- `QuickSortTest.kt` (52 lines) - 8 sort correctness tests
- `MergeSortTest.kt` (52 lines) - 8 sort correctness tests
- `SortVisualizationTest.kt` (57 lines) - 5 visualizer event emission tests

## Tasks Completed
- [x] BubbleSort + BubbleSortVisualizer with Compare/Swap events
- [x] SelectionSort + SelectionSortVisualizer with Compare/Select/Deselect/Swap events
- [x] InsertionSort + InsertionSortVisualizer with Compare/Overwrite events
- [x] QuickSort + QuickSortVisualizer with Pivot/Compare/Swap events (deterministic pivot)
- [x] MergeSort + MergeSortVisualizer with Compare/Overwrite events
- [x] Sort correctness tests (empty, single, sorted, reverse, duplicates, random, strings)
- [x] Visualization tests (Start/Complete event verification, replayCache approach)
- [x] Build config updated with coroutines test deps

## Tests Status
- Type check: pass (no warnings except linter-formatting-induced no-ops)
- Unit tests: pass (88 total - 83 existing + 5 new viz tests)
- `./gradlew :algo-core:check` BUILD SUCCESSFUL

## Design Decisions
- QuickSortVisualizer uses deterministic last-element pivot (not random) for predictable visualization
- MergeSort pure sort uses in-place temp slice approach matching Java original
- Visualization tests use `MutableSharedFlow(replay = Int.MAX_VALUE)` + `replayCache` to avoid collector race conditions
- All SortAlgorithm impls use generic `T : Comparable<T>`; VisualizableAlgorithm uses `List<Int>` per existing interface

## Issues Encountered
- Linter repeatedly reformatted SortVisualizationTest.kt, requiring bash `cat` writes to avoid race
- Kotest FunSpec test bodies are already coroutines - `= runTest` syntax is incompatible
- MutableSharedFlow collector race: `launch` + `toList` approach fails because collector never runs before cancel; solved by reading `replayCache` directly
- MergeSort.kt is 103 lines (3 over 100-line target) due to dual class+visualizer with suspend merge helpers

## Next Steps
- Phase 06 (Desktop UI) can now consume all 5 sort visualizers
- Phase 05 (Search algorithms) unblocked by shared interfaces
