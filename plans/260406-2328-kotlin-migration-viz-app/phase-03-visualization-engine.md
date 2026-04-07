# Phase 03: Visualization Engine — AlgorithmPlayer, Playback, Event Pipeline

**Date:** 2026-04-07
**Priority:** P1
**Status:** [ ] Pending

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: Phase 02 (core module interfaces + events)
- Research: [researcher-01-compose-viz-architecture.md](research/researcher-01-compose-viz-architecture.md) — Streaming + Hybrid pattern
- Consumes: `VisualizableAlgorithm`, `AlgorithmEvent`, `PlaybackState` from `algo-shared`

## Overview

Build the visualization engine that sits between algorithms (event producers) and the UI (event consumer). Implements `AlgorithmPlayer` with streaming playback via `SharedFlow`, event buffering for step-back, speed control, and snapshot-based state reconstruction.

## Key Insights

- **Streaming pattern chosen**: algorithms emit events in real-time; engine buffers for replay.
- **Hybrid approach**: stream events live, but also create periodic snapshots (every 10 events) for efficient step-back without replaying from event 0.
- **Speed control**: delay between events adjusted via `MutableStateFlow<Long>` (ms per event). Range: 10ms (fast) to 2000ms (slow).
- **Single source of truth**: `StateFlow<PlaybackState>` drives UI. UI never directly controls algorithm execution.
- Event buffer is bounded — max 10,000 events to prevent OOM on pathological inputs.

## Requirements

### Functional
- `AlgorithmPlayer` class:
  - `suspend fun run(algorithm: VisualizableAlgorithm, input: List<Int>)` — execute and collect all events
  - `fun play()`, `fun pause()`, `fun stop()`
  - `fun stepForward()`, `fun stepBack()`
  - `fun setSpeed(msPerEvent: Long)` — range 10..2000
  - `val state: StateFlow<PlaybackState>` — observable state
  - `val currentSnapshot: StateFlow<AlgorithmSnapshot>` — current visual state
  - `val currentEventIndex: StateFlow<Int>` — position in event stream
- `AlgorithmSnapshot` data class: array state + highlighted indices + metadata
- Event buffer with max capacity (10,000 events)
- Snapshot creation every N events (configurable, default 10)
- `SnapshotReconstructor` — rebuilds array state from snapshot + delta events

### Non-Functional
- O(1) step-forward after initial replay
- O(N) step-back where N = events since last snapshot (max 10)
- Zero UI framework dependencies (pure Kotlin + coroutines)
- Thread-safe: all state mutations via `MutableStateFlow` on single coroutine context

## Architecture

### Component Diagram

```
VisualizableAlgorithm
       │
       │ emit(AlgorithmEvent)
       ▼
 MutableSharedFlow<AlgorithmEvent>
       │
       ▼
  EventCollector (coroutine)
       │
       ├─→ EventBuffer (List<AlgorithmEvent>)
       ├─→ SnapshotStore (Map<Int, AlgorithmSnapshot>)
       └─→ StateFlow<PlaybackState> updates
              │
              ▼
        PlaybackController
       ┌───┼───────────┐
       │   │           │
    play() pause()  stepForward()
                      stepBack()
              │
              ▼
     StateFlow<AlgorithmSnapshot> → UI
```

### Data Flow (Step-Back)

```
User clicks stepBack
    ↓
PlaybackController decrements currentIndex
    ↓
Find nearest snapshot ≤ currentIndex
    ↓
Reconstruct: snapshot state + replay events from snapshot to currentIndex
    ↓
Emit new AlgorithmSnapshot to StateFlow
```

## Related Code Files

### Create (algo-viz-engine)
- `algo-viz-engine/src/commonMain/kotlin/com/thealgorithms/viz/AlgorithmPlayer.kt`
- `algo-viz-engine/src/commonMain/kotlin/com/thealgorithms/viz/AlgorithmSnapshot.kt`
- `algo-viz-engine/src/commonMain/kotlin/com/thealgorithms/viz/SnapshotReconstructor.kt`
- `algo-viz-engine/src/commonMain/kotlin/com/thealgorithms/viz/PlaybackController.kt`
- `algo-viz-engine/src/commonMain/kotlin/com/thealgorithms/viz/EventBuffer.kt`
- `algo-viz-engine/src/commonTest/kotlin/com/thealgorithms/viz/AlgorithmPlayerTest.kt`
- `algo-viz-engine/src/commonTest/kotlin/com/thealgorithms/viz/SnapshotReconstructorTest.kt`
- `algo-viz-engine/src/commonTest/kotlin/com/thealgorithms/viz/EventBufferTest.kt`
- `algo-viz-engine/src/commonTest/kotlin/com/thealgorithms/viz/MockVisualizableAlgorithm.kt` (test helper)

### Modify
- `algo-viz-engine/build.gradle.kts` — add coroutines + Kotest deps

### Delete
- None

## Implementation Steps

1. Create `AlgorithmSnapshot.kt` — data class with `arrayState: List<Int>`, `highlightedIndices: Set<Int>`, `comparisons: Int`, `swaps: Int`
2. Create `EventBuffer.kt` — ring buffer with max capacity, stores `AlgorithmEvent` list
3. Create `SnapshotReconstructor.kt` — given a snapshot + list of events, produces new snapshot
4. Create `PlaybackController.kt` — manages play/pause/stop/step state machine
5. Create `AlgorithmPlayer.kt` — orchestrates algorithm execution, event collection, buffering, snapshotting
6. Add coroutines + Kotest deps to `algo-viz-engine/build.gradle.kts`
7. Create `MockVisualizableAlgorithm.kt` — emits a known sequence of events for testing
8. Write `EventBufferTest` — capacity limits, add/retrieve, overflow behavior
9. Write `SnapshotReconstructorTest` — reconstruct from known snapshots + events
10. Write `AlgorithmPlayerTest` — full playback cycle: run → play → pause → stepForward → stepBack → stop
11. Verify: `./gradlew :algo-viz-engine:check` passes

## Todo List

- [ ] Create `AlgorithmSnapshot.kt`
- [ ] Create `EventBuffer.kt`
- [ ] Create `SnapshotReconstructor.kt`
- [ ] Create `PlaybackController.kt`
- [ ] Create `AlgorithmPlayer.kt`
- [ ] Update `algo-viz-engine/build.gradle.kts` with deps
- [ ] Create `MockVisualizableAlgorithm.kt` test helper
- [ ] Write `EventBufferTest`
- [ ] Write `SnapshotReconstructorTest`
- [ ] Write `AlgorithmPlayerTest`
- [ ] Verify `./gradlew :algo-viz-engine:check` passes

## Success Criteria

- [ ] `AlgorithmPlayer.run()` collects all events from a mock algorithm
- [ ] Step-forward advances `currentEventIndex` by 1
- [ ] Step-back decrements `currentEventIndex` by 1 and reconstructs correct snapshot
- [ ] Speed control changes delay between events
- [ ] Event buffer caps at 10,000 events, drops oldest on overflow
- [ ] All tests pass: `./gradlew :algo-viz-engine:check`

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Coroutine cancellation leaves player in inconsistent state | Medium | High | Use `Mutex` for state mutations; `withContext(Dispatchers.Default)` for computation |
| Snapshot reconstruction accumulates floating-point errors | Low | Low | We use `Int` arrays — no floating point |
| Large event streams (>10K) cause memory pressure | Medium | Medium | Bounded buffer; LRU eviction for snapshots |
| Race between play() and stepForward() | Medium | Medium | Single-threaded dispatcher for playback commands |

## Security Considerations

- None (no I/O, no network, no file access)

## Next Steps

- Phase 04 (Sorting MVP) provides real `VisualizableAlgorithm` implementations
- Phase 06 (Desktop UI) consumes `StateFlow<AlgorithmSnapshot>` for rendering
