# Phase 01: Data Models + CompareViewModel

## Context
- Parent: [plan.md](./plan.md)
- Brainstorm: [brainstorm-260408-compare-mode.md](../reports/brainstorm-260408-compare-mode.md)
- No dependencies — first phase

## Overview
- **Date**: 2026-04-08
- **Priority**: P1
- **Status**: Pending
- **Effort**: 2h

Create data models and the CompareViewModel that orchestrates 4 synchronized AlgorithmPlayer instances.

## Key Insights
- `AlgorithmPlayer` is reusable as-is — it's stateless between runs, each instance independent
- Current `AppViewModel` = 1 player, 1 snapshot, 1 playback state. Compare = 4 of each.
- `ViewMode` enum lets AppContent switch between layouts cleanly
- Synchronized playback means calling `stepForward()` on all 4 players per tick
- Different algorithms have different total event counts — some finish before others

## Requirements
- `ViewMode` enum: SINGLE, COMPARE
- `CompareSlotState` data class: algorithm info, snapshot, event counts per slot
- `CompareViewModel`: 4 AlgorithmPlayer instances, synchronized play/pause/step/stop
- Shared input array + speed across all 4 slots
- Expose `slots: StateFlow<List<CompareSlotState>>` for UI
- Expose shared playback state (playing/paused/stopped/complete)
- `runAll()` runs all 4 algorithms on same input
- Playback continues until ALL slots are complete

## Architecture

### ViewMode Enum
```kotlin
// algo-ui-shared/.../model/ViewMode.kt
enum class ViewMode { SINGLE, COMPARE }
```

### CompareSlotState
```kotlin
// algo-ui-shared/.../ui/model/CompareSlotState.kt
data class CompareSlotState(
    val algorithm: AlgorithmInfo,
    val snapshot: AlgorithmSnapshot = AlgorithmSnapshot(emptyList()),
    val playbackState: PlaybackState = PlaybackState.Stopped,
    val eventIndex: Int = 0,
    val totalEvents: Int = 0,
    val comparisons: Int = 0,
    val swaps: Int = 0,
)
```

### CompareViewModel
```kotlin
// algo-ui-shared/.../ui/CompareViewModel.kt
class CompareViewModel {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val players = List(4) { AlgorithmPlayer() }
    private val playerJobs = mutableListOf<Job?>()

    private val _selectedAlgorithms = MutableStateFlow<List<AlgorithmInfo>>(emptyList())
    val selectedAlgorithms: StateFlow<List<AlgorithmInfo>> = _selectedAlgorithms.asStateFlow()

    private val _inputArray = MutableStateFlow<List<Int>>(listOf(5, 3, 1, 4, 2))
    val inputArray: StateFlow<List<Int>> = _inputArray.asStateFlow()

    private val _speedMs = MutableStateFlow(500L)
    val speedMs: StateFlow<Long> = _speedMs.asStateFlow()

    private val _slots = MutableStateFlow<List<CompareSlotState>>(emptyList())
    val slots: StateFlow<List<CompareSlotState>> = _slots.asStateFlow()

    // Synchronized playback state — derived from all players
    val playbackState: StateFlow<PlaybackState> = combine(/* all 4 player states */) { ... }
    val progress: StateFlow<Pair<Int, Int>> = /* min eventIndex, max totalEvents */

    fun selectAlgorithms(algorithms: List<AlgorithmInfo>) {
        require(algorithms.size == 4)
        _selectedAlgorithms.value = algorithms
    }

    fun setInputArray(array: List<Int>) { _inputArray.value = array }
    fun setSpeed(ms: Long) { _speedMs.value = ms.coerceIn(10L, 2000L) }

    fun runAll() {
        // Run all 4 algorithms in parallel, each on its own player
        scope.launch {
            val algorithms = _selectedAlgorithms.value
            if (algorithms.size != 4) return@launch
            val input = _inputArray.value

            // Run all 4 concurrently
            algorithms.mapIndexed { i, algo ->
                launch {
                    players[i].run(
                        AlgorithmRegistry.visualizerAsSort(algo),
                        input
                    )
                }
            }.joinAll()

            updateSlots()
        }
    }

    // Synchronized: all advance one event per tick
    fun play() { /* launch coroutine that steps all 4 forward per tick */ }
    fun pause() { /* cancel playback job, mark paused */ }
    fun stop() { /* stop all 4 players, reset to event 0 */ }
    fun stepForward() { /* stepForward on all 4 */ }
    fun stepBack() { /* stepBack on all 4 */ }

    fun destroy() { players.forEach { it.destroy() }; scope.cancel() }

    private fun updateSlots() {
        _slots.value = _selectedAlgorithms.value.mapIndexed { i, algo ->
            CompareSlotState(
                algorithm = algo,
                snapshot = players[i].currentSnapshot.value,
                playbackState = players[i].state.value,
                eventIndex = players[i].currentEventIndex.value,
                totalEvents = players[i].totalEvents.value,
                comparisons = players[i].currentSnapshot.value.comparisons,
                swaps = players[i].currentSnapshot.value.swaps,
            )
        }
    }
}
```

### Synchronized Playback Logic
```kotlin
private var playbackJob: Job? = null

fun play() {
    if (_slots.value.isEmpty()) return
    _isPlaying.value = true

    playbackJob?.cancel()
    playbackJob = scope.launch {
        while (true) {
            var anyAdvanced = false
            players.forEachIndexed { i, player ->
                val state = player.state.value
                if (state !is PlaybackState.Complete) {
                    player.stepForward()
                    anyAdvanced = true
                }
            }
            updateSlots()
            if (!anyAdvanced) {
                _isPlaying.value = false
                break
            }
            delay(_speedMs.value)
        }
    }
}

fun pause() {
    playbackJob?.cancel()
    _isPlaying.value = false
}

fun stop() {
    playbackJob?.cancel()
    players.forEach { it.stop() }
    _isPlaying.value = false
    updateSlots()
}

fun stepForward() {
    playbackJob?.cancel()
    _isPlaying.value = false
    players.forEach { player ->
        if (player.state.value !is PlaybackState.Complete) {
            player.stepForward()
        }
    }
    updateSlots()
}

fun stepBack() {
    playbackJob?.cancel()
    _isPlaying.value = false
    players.forEach { it.stepBack() }
    updateSlots()
}
```

## Related Code Files

### Create
| File | Purpose |
|------|---------|
| `algo-ui-shared/.../model/ViewMode.kt` | SINGLE / COMPARE enum |
| `algo-ui-shared/.../ui/model/CompareSlotState.kt` | Per-slot state |
| `algo-ui-shared/.../ui/CompareViewModel.kt` | 4-player orchestration |

### Modify
| File | Change |
|------|--------|
| None in this phase | — |

## Implementation Steps

1. Create `ViewMode.kt` enum
2. Create `CompareSlotState.kt` data class
3. Create `CompareViewModel.kt` with:
   - 4 AlgorithmPlayer instances
   - selectAlgorithms(), setInputArray(), setSpeed()
   - runAll() — concurrent execution
   - Synchronized play/pause/stop/stepForward/stepBack
   - slots StateFlow updated after each step
   - playbackState derived from all players
   - progress (min index / max total)
4. Build verify

## Todo
- [ ] Create ViewMode.kt
- [ ] Create CompareSlotState.kt
- [ ] Create CompareViewModel.kt with synchronized playback
- [ ] Build verify: `./gradlew :algo-ui-shared:compileKotlinWasmJs`

## Success Criteria
- [ ] CompareViewModel compiles
- [ ] 4 independent AlgorithmPlayer instances
- [ ] Synchronized stepForward advances all non-complete players
- [ ] Slots state updates after each step
- [ ] No changes to existing AppViewModel or algo-viz-engine

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| 4 concurrent player runs = race conditions | Each player is independent, no shared mutable state |
| Slot state gets stale | updateSlots() called after every step/delay |
| Different event counts = confusing progress | Show per-slot progress, global progress = min/max |

## Next Steps
- Phase 02: UI components (ComparePanel, CompareSlot, AlgorithmPickerDialog)
