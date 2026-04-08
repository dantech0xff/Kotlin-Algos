---
title: "Educational Excellence — Polish 14 Algorithms"
description: "Transform current algorithm visualizer into best-in-class learning tool with pseudocode, step explanations, rich metadata, and UI polish"
status: completed
priority: P1
effort: 12h
branch: master
tags: [feature, frontend, educational, ui-polish]
created: 2026-04-08
---

# Educational Excellence Plan

## Brainstorm Report
→ [brainstorm-260408-educational-excellence.md](../reports/brainstorm-260408-educational-excellence.md)

## Goal
Make every algorithm **self-explaining** — a student learns from the tool alone, no external references needed.

## Phases

| # | Phase | Status | Effort | Link |
|---|-------|--------|--------|------|
| 1 | Foundation: Event API + Color Theme + Data Models | ✅ Completed | 2h | [phase-01](./phase-01-foundation.md) |
| 2 | Algorithm Descriptions (all 14) | ✅ Completed | 3h | [phase-02](./phase-02-algorithm-descriptions.md) |
| 3 | Info Panel Components (pseudocode + explanation + complexity + legend) | ✅ Completed | 3h | [phase-03](./phase-03-info-panels.md) |
| 4 | Three-Column Layout Integration | ✅ Completed | 1.5h | [phase-04](./phase-04-layout-integration.md) |
| 5 | Interaction Polish (presets, scrubber, sidebar, hover, celebration) | ✅ Completed | 2.5h | [phase-05](./phase-05-interaction-polish.md) |

## Dependencies
```
Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5
```
All phases strictly sequential. Phase 2 before 3 so InfoPanel components can be tested with real descriptions.

## Key Architecture Decisions
- **Event-carried descriptions**: Each `AlgorithmEvent` has `description: String`
- **Bake into snapshot**: `AlgorithmSnapshot` carries `currentDescription` + `activePseudocodeLine`
- **Pseudocode format**: `List<PseudocodeLine>` with `text + indentLevel`
- **Three-column layout**: Nav (200dp) | Viz (flex) | Info (280dp)
- **Centralized colors**: Single `VizColors` object, no private constants
- **Complete event description**: `Complete` event gets `description` field for end-of-run narrative
- **Cross-platform testing**: Test both Desktop + Web (Wasm) after each phase

## Validation Summary

**Validated:** 2026-04-08
**Questions asked:** 6

### Confirmed Decisions
- Q1: Event-carried descriptions (not reconstructor-generated) — more expressive per-algorithm
- Q2: Bake description + pseudocodeLine into AlgorithmSnapshot — natural with seek/scrub
- Q3: **Sequential** Phase 2 → Phase 3 (changed from parallel) — InfoPanel testable with real data
- Q4: Ship all 5 phases together — maximum polish on first release
- Q5: Add description to Complete event — better end-of-run narrative
- Q6: Test Desktop + Web after each phase — catches platform issues early

### Action Items
- [x] Update dependency graph: sequential only, no parallelism
- [x] Phase 01: Add `description: String` to `Complete` event (was not in original plan)
- [x] Phase 02: Write completion descriptions for all 14 algorithms
- [x] All phases: include Desktop + Wasm test verification step

## Code Review

→ [code-reviewer-260408-educational-excellence.md](../reports/code-reviewer-260408-educational-excellence.md)

**Verdict: FIX FIRST**

### Critical (must fix before ship)
- [ ] C1: Swap description ordering — 4 files emit description AFTER `swapAt()` mutates array (BubbleSort, CocktailSort, QuickSort, SelectionSort)

### Major (should fix soon)
- [ ] M1: CocktailSort pseudocode lines 6,10 emitted but not defined in pseudocode
- [ ] M2: `Color.Gray` hardcoded in StatsPanel line 110
- [ ] M3: `parseArray` filters `it > 0` — blocks 0 and negative values
- [ ] M4: `Color.White`/`Color.Black` hardcoded in SortVisualization (3 locations)

### Minor (nice to fix)
- [ ] m1: Enrich `DescriptionUtils.swap()` with "since X > Y" explanation
- [ ] m2: Add description/pseudocodeLine to Select/Deselect events
- [ ] m3: Deprecate `highlightedIndices` legacy accessor
- [ ] m4: Inline descriptions in MergeSort/CycleSort inconsistent with DescriptionUtils pattern
