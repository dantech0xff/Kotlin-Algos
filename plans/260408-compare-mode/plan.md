---
title: "Compare Mode — 4 Algorithms Side-by-Side"
description: "Synchronized 2×2 grid comparing 4 sort algorithms on same input"
status: pending
priority: P1
effort: 8h
branch: master
tags: [feature, frontend, educational, compare]
created: 2026-04-08
---

# Compare Mode Plan

## Brainstorm Report
→ [brainstorm-260408-compare-mode.md](../reports/brainstorm-260408-compare-mode.md)

## Goal
Students see 4 sort algorithms run on the **same input at the same step** — the #1 feature request for algorithm education.

## Phases

| # | Phase | Status | Effort | Link |
|---|-------|--------|--------|------|
| 1 | Data Models + CompareViewModel | Pending | 2h | [phase-01](./phase-01-data-models-viewmodel.md) |
| 2 | Compare UI Components (picker, panel, slot) | Pending | 3h | [phase-02](./phase-02-ui-components.md) |
| 3 | Integration + Mode Toggle | Pending | 2h | [phase-03](./phase-03-integration.md) |
| 4 | Testing + Polish | Pending | 1h | [phase-04](./phase-04-testing-polish.md) |

## Dependencies
```
Phase 1 → Phase 2 → Phase 3 → Phase 4
```
All sequential.

## Key Architecture Decisions
- **Synchronized playback**: All 4 players advance one event per tick
- **Separate CompareViewModel**: Zero risk to single-algorithm view
- **Sort algorithms only**: No search in compare mode
- **2×2 fixed grid**: Exactly 4 slots, user picks 4 algorithms
- **No info panel in compare**: Mini stats per slot only
- **AlgorithmPlayer reused as-is**: No changes to algo-viz-engine
