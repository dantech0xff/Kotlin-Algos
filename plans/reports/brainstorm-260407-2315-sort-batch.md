# Brainstorm: Next Algorithm Batch — Sort-Heavy

**Date:** 2026-04-07 23:15
**Status:** Agreed

## Problem Statement
Add more sort algorithms to the visualizer. Currently 5 sorts + 3 searches. Target: 6 new sorts for maximum educational/visual variety.

## Architecture
- Pattern: `XSort` (pure logic) + `XSortVisualizer` (emit `AlgorithmEvent`s) + registry entry + tests
- No new event types needed — existing events cover all 6 algorithms
- No UI changes needed — sorts work on any input

## Evaluated Approaches

### A: Sort-Heavy Batch ✅ SELECTED
6 new sorts (HeapSort, ShellSort, CountingSort, CocktailSort, CycleSort, RadixSort). Zero UX changes. ~4-6h effort.

### B: Search-Heavy ❌ DEFERRED
4 new searches. Needs sorted-input UX validation first. Medium risk.

### C: Mixed Batch ❌ REJECTED
Approach A is cleaner — same category, same pattern, no UX gaps.

## Implementation Order
1. HeapSort (Compare, Swap, Overwrite) — most requested viz
2. ShellSort (Compare, Swap with gap) — visually unique
3. CountingSort (Overwrite, Select) — non-comparison paradigm
4. CocktailSort (Compare, Swap) — bidirectional bubble, trivial
5. CycleSort (Compare, Overwrite, Swap) — minimal writes
6. RadixSort (Overwrite, Select) — digit-by-digit

## Per-Algorithm Checklist
1. `algo-core/src/commonMain/kotlin/.../sorts/XSort.kt` — pure + visualizer classes
2. `algo-core/src/jvmTest/kotlin/.../sorts/XSortTest.kt` — correctness tests
3. Visualization event test added to `SortVisualizationTest.kt`
4. One-line entry in `AlgorithmRegistry.algorithms`

## Risks
- Low: Same pattern as existing 5 sorts
- RadixSort needs careful digit extraction on Wasm (no java.lang.Math)
- CountingSort needs array size cap (large ranges = memory issues)

## Unresolved Questions
- Max input array size for CountingSort (suggest cap at range 0..100)
- Whether to add complexity info card to UI (defer to polish phase)
