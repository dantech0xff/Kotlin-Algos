---
title: "Fix Compare Mode Playback Animation"
description: "Fix 3 interacting bugs preventing animation in 2x2 compare mode"
status: pending
priority: P1
effort: 4h
branch: master
tags: [bug, compare-mode, animation, wasm, state-flow]
created: 2026-04-08
---

# Fix Compare Mode Playback Animation

## Problem

Compare mode (4 algorithms in 2x2 grid) renders UI but playback animation doesn't work. After Run → Play, no animation occurs. Single-algorithm mode works fine.

## Root Cause — 3 Interacting Bugs

1. **Player state stuck at `Stopped`**: `AlgorithmPlayer.run()` calls `stop()` first, never transitions to `Paused` after populating buffer. `stepForward()` only sets `Complete`, never `Paused`.
2. **Stale StateFlow reads**: `CompareViewModel.play()` mutates player StateFlows then reads them imperatively in same coroutine. WASM single-threaded event loop → stale values.
3. **No yield points**: `runAll()` runs 4 algorithms in one coroutine with no `yield()`. WASM can't recompose until entire block finishes.

## Solution Summary

- **Phase 1**: Fix `AlgorithmPlayer` — add `runAndPrepare()`, fix `stepForward()` state transitions
- **Phase 2**: Rewrite `CompareViewModel` — reactive `slots` via `combine()`, delegate to `player.play()`, remove `refreshSlots()`
- **Phase 3**: Error handling + `ComparePanel` updates

## Files to Modify

| File | Action | Phase |
|------|--------|-------|
| `algo-viz-engine/.../AlgorithmPlayer.kt` | MODIFY | 1 |
| `algo-ui-shared/.../CompareViewModel.kt` | REWRITE | 2 |
| `algo-ui-shared/.../ComparePanel.kt` | MODIFY | 3 |

## Phases

| # | File | Effort | Description |
|---|------|--------|-------------|
| 1 | [phase-01](phase-01-algorithm-player-fix.md) | 1h | Fix player state machine |
| 2 | [phase-02](phase-02-compare-viewmodel-rewrite.md) | 2h | Reactive slots, delegate playback |
| 3 | [phase-03](phase-03-error-handling.md) | 1h | Error handling, UI updates |

## Dependencies

- Phase 2 depends on Phase 1 (`runAndPrepare()` must exist)
- Phase 3 depends on Phase 2 (error types from rewrite)
- Phases must execute sequentially

## Success Criteria

- [ ] Run → Play animates all 4 algorithm slots in compare mode
- [ ] Pause/Stop/StepForward/StepBack work correctly
- [ ] Speed control affects all 4 players
- [ ] State transitions: Stopped → Paused (after Run) → Playing → Paused/Complete
- [ ] No stale state on WASM target
- [ ] Single-algorithm mode unaffected

## Unresolved Questions

- None. Root cause fully identified, solution approved.
