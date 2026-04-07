---
title: "Java → Kotlin Migration + Algorithm Visualization App"
description: "Migrate Java algorithms to Kotlin and build Compose Desktop visualization app"
status: complete
priority: P2
effort: 8h
branch: master
tags: [kotlin, compose-desktop, algorithms, visualization]
created: 2026-04-07
---

# Java → Kotlin Migration + Algorithm Visualization App

## Goal
Migrate 8 MVP algorithms (5 sorting + 3 search) from Java to idiomatic Kotlin and build a Compose Desktop app that visualizes them step-by-step with playback controls.

## Module Structure
```
algo-shared    → interfaces, AlgorithmEvent sealed class, PlaybackState
algo-core      → algorithm implementations, SortUtils/SearchUtils extensions, tests
algo-viz-engine→ AlgorithmPlayer, event buffer, snapshot reconstructor, playback controller
algo-ui-desktop→ Compose Desktop app (Material 3, Canvas rendering, keyboard shortcuts)
```

## Dependency Graph
```
Phase 01 (Setup)
    ↓
Phase 02 (Core Module)
    ↓
Phase 03 (Viz Engine)
    ↓
Phase 04 (Sorting) + Phase 05 (Search)  ← parallel
    ↓
Phase 06 (Desktop UI)
    ↓
Phase 07 (Testing & Polish)
```

## Phase Summary

| Phase | File | Scope | Est. | Status |
|-------|------|-------|------|--------|
| 01 | [phase-01-project-setup.md](phase-01-project-setup.md) | Gradle multi-module, Compose scaffold | 1h | [x] |
| 02 | [phase-02-core-module.md](phase-02-core-module.md) | Interfaces, AlgorithmEvent, SortUtils, test base | 1.5h | [x] |
| 03 | [phase-03-visualization-engine.md](phase-03-visualization-engine.md) | AlgorithmPlayer, snapshots, playback | 1.5h | [x] |
| 04 | [phase-04-sorting-algorithms.md](phase-04-sorting-algorithms.md) | Bubble/Selection/Insertion/Quick/Merge Sort | 1.5h | [x] |
| 05 | [phase-05-search-algorithms.md](phase-05-search-algorithms.md) | LinearSearch, BinarySearch (iter+rec) | 1h | [x] |
| 06 | [phase-06-desktop-ui.md](phase-06-desktop-ui.md) | Compose Desktop app, all panels, controls | 1.5h | [x] |
| 07 | [phase-07-testing-polish.md](phase-07-testing-polish.md) | Integration tests, README, demo | 0.5h | [x] |

## MVP Algorithms
**Sorting:** BubbleSort, SelectionSort, InsertionSort, QuickSort, MergeSort
**Search:** LinearSearch, IterativeBinarySearch, RecursiveBinarySearch

## Key Architecture Decisions
- Streaming events via `MutableSharedFlow<AlgorithmEvent>` with snapshot-based step-back
- Kotlin `List<T>` API (not arrays) for idiomatic code
- Generic `T : Comparable<T>` from start (validated)
- Kotest for testing, Material 3 for UI, Canvas API for visualization
- Gradle Kotlin DSL, version catalog, JVM toolchain 21
- Replace Maven entirely with Gradle (validated)
- Dual interface: SortAlgorithm + VisualizableAlgorithm per algorithm (validated)
- SearchInput data class for search algorithms (validated)
- Live code execution pointer in code viewer (validated)

## Validation Summary

**Validated:** 2026-04-07
**Questions asked:** 5

### Confirmed Decisions
- Dual interface (SortAlgorithm + VisualizableAlgorithm): Keep as planned
- Search input: **Override** — use `SearchInput(array, key)` data class instead of last-element-as-key
- Generic types: **Override** — use `T : Comparable<T>` from start, not Int-only
- Code viewer: **Override** — live execution pointer highlighting, not static text
- Build system: **Override** — replace Maven entirely with Gradle

### Action Items
- [x] Phase 01: Migrate entire build to Gradle (remove pom.xml)
- [x] Phase 02: Use generic `T : Comparable<T>` in all interfaces and SortUtils extensions
- [x] Phase 05: Create `SearchInput` data class in algo-shared, update VisualizableAlgorithm for search
- [x] Phase 06: Add event→source line mapping for live code execution pointer
- [x] Update effort estimate: ~10h (increased from 8h due to generics + code pointer + Maven removal)

## Rollback
Each phase independently revertible via git revert.

## Unresolved Questions
- Event granularity tuning (too many = perf issues, too few = poor viz)
- Distributable packaging (.dmg/.exe) deferred to post-MVP
