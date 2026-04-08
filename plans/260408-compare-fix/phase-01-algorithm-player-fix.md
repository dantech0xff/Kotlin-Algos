---
title: "Phase 1: Fix AlgorithmPlayer State Machine"
description: "Add runAndPrepare() and fix stepForward() state transitions"
status: pending
priority: P1
effort: 1h
phase: 1 of 3
---

# Phase 1: Fix AlgorithmPlayer State Machine

## Context

`AlgorithmPlayer.run()` calls `stop()` internally → `_state = Stopped`. After populating buffer, state remains `Stopped`. Should be `Paused` (ready to play). `stepForward()` only sets `Complete` at end, never `Paused` between steps.

## Overview

Add `runAndPrepare()` that wraps `run()` and transitions to `Paused`. Fix `stepForward()` to set `Paused` after each advance, `Complete` only at last event.

## File

`algo-viz-engine/src/commonMain/kotlin/com/thealgorithms/viz/AlgorithmPlayer.kt`

## Implementation Steps

### Step 1: Add `runAndPrepare()` method

```kotlin
suspend fun runAndPrepare(algorithm: Algorithm, input: InputType) {
    run(algorithm, input)
    // After run() populates buffer, transition to Paused (ready to play)
    if (_events.value.isNotEmpty()) {
        _state.value = PlaybackState.Paused
    }
}
```

- Place after existing `run()` method (~line 70)
- Reuses existing `run()` logic — no duplication
- Only changes state if events exist (empty algorithm → stays Stopped)

### Step 2: Fix `stepForward()` state transitions

Current behavior (lines ~148-155):
- Increments index
- Sets `Complete` only when index reaches last event
- **Never sets `Paused`**

New behavior:
```kotlin
fun stepForward() {
    val current = _currentEventIndex.value
    val total = _events.value.size
    if (current < total - 1) {
        _currentEventIndex.value = current + 1
        if (_currentEventIndex.value >= total - 1) {
            _state.value = PlaybackState.Complete
        } else {
            _state.value = PlaybackState.Paused
        }
    }
}
```

- After advancing: `Paused` (not Stopped)
- At last event: `Complete` (unchanged)
- At first event after `runAndPrepare()`: already `Paused`, stays `Paused`

### Step 3: Verify `stepBackward()` consistency

Check `stepBackward()` sets `Paused` when stepping back from `Complete`. If not, add same fix:
```kotlin
fun stepBackward() {
    val current = _currentEventIndex.value
    if (current > 0) {
        _currentEventIndex.value = current - 1
        _state.value = PlaybackState.Paused  // Always Paused after step back
    }
}
```

## Success Criteria

- [ ] `runAndPrepare()` sets state to `Paused` (not `Stopped`) after populating buffer
- [ ] `stepForward()` sets `Paused` between steps, `Complete` only at last event
- [ ] `stepBackward()` sets `Paused` after stepping back
- [ ] Existing `play()`, `pause()`, `stop()` unaffected
- [ ] State machine: Stopped → (runAndPrepare) → Paused → (play) → Playing → (step) → Paused/Complete
- [ ] Single-algorithm mode still works (uses `run()` directly, behavior unchanged)

## Risk Assessment

| Risk | Likelihood | Mitigation |
|------|-----------|------------|
| Single-algo mode breaks | Low | `run()` unchanged, only `runAndPrepare()` is new |
| State machine edge case | Medium | Test: step forward from last event, step back from first |
| `play()` behavior change | Low | `play()` already handles `Paused` → `Playing` transition |
