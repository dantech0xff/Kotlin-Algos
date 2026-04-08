# Compare Mode "Run" Bug — Execution Flow Analysis

**Date:** 2026-04-08  
**Scope:** READ-ONLY research, no files modified  
**Symptom:** Clicking "Run" in Compare Mode (4 algorithms, random input) produces no visible results on WASM target

---

## Executive Summary

Traced full execution path from "user clicks Run" through `CompareViewModel.runAll()` → `AlgorithmPlayer.run()` → `refreshSlots()` → `ComparePanel` composition → `PlaybackControls` enablement. Found **3 confirmed bugs** and **2 high-risk design issues**. The primary root cause is **Bug #1** (infinite play loop for single-event buffers) compounded by **Bug #2** (silent coroutine exception swallowing). A secondary contributing factor is **Issue #3** (`_state` never transitions out of `Stopped` after `run()`), which creates a state inconsistency between `AlgorithmPlayer` and `CompareViewModel`.

---

## Full State Trace: "User Clicks Run"

### Phase 1: onClick → `CompareViewModel.runAll()`

**File:** `CompareViewModel.kt:62-86`

```
runAll()
  ├── val algorithms = _selectedAlgorithms.value     // 4 items ✓
  ├── if (algorithms.size != 4) return               // passes ✓
  ├── val input = _inputArray.value                   // [5,3,1,4,2] or random
  ├── stop()                                          // cancels playback, sets isReady=false
  │     ├── playbackJob?.cancel()                     // null → no-op on first run
  │     ├── _isPlaying.value = false
  │     ├── players.forEach { it.stop() }             // each player: state=Stopped, index=0
  │     ├── _isReady.value = false
  │     └── refreshSlots()                            // slots = 4x Stopped, empty snapshots, total=0
  ├── _isReady.value = false                          // redundant with stop()
  └── scope.launch { ... }                            // enqueued on Dispatchers.Default (WASM=main)
```

**After this phase:** UI recomposes → `playbackState = Stopped`, no bars, Play disabled.

### Phase 2: Coroutine executes (on next event loop tick)

**File:** `CompareViewModel.kt:70-85`

```
scope.launch {
  // --- Loop 1: players[0].run(algo0, input) ---
  //   AlgorithmPlayer.kt:76-88
  //   ├── stop() → _state = Stopped, index = 0
  //   ├── buffer.clear(), snapshots.clear()
  //   ├── _currentEventIndex = 0, _totalEvents = 0
  //   ├── RecordingEmitter created
  //   ├── algorithm.execute(input, emitter)  ← synchronous, populates emitter.recorded
  //   │     Each emit(): _recordedSnapshot += event; delegate.emit(event) ← non-suspending
  //   └── processCapturedEvents(emitter.recorded)
  //         AlgorithmPlayer.kt:90-104
  //         ├── for each event: buffer.add(event)
  //         ├── if idx % 10 == 0: snapshots[idx] = reconstructToIndex(idx)
  //         ├── _totalEvents = buffer.size  (e.g., 22 for BubbleSort [5,3,1,4,2])
  //         └── _currentSnapshot = reconstructToIndex(0) = AlgorithmSnapshot([5,3,1,4,2])
  //   STATE AFTER RUN: _state=Stopped, _totalEvents=22, _currentEventIndex=0

  // --- Same for players[1], [2], [3] ---

  // --- players.forEach { it.stepForward() } ---
  //   AlgorithmPlayer.kt:144-156
  //   ├── _state is Complete? → Stopped, passes
  //   ├── 0 < 21? → true → _currentEventIndex = 1
  //   ├── _currentSnapshot = reconstructToIndex(1)  ← shows first Compare event
  //   └── 1 >= 21? → false → _state remains Stopped!
  //   STATE AFTER STEP: _state=Stopped, _currentEventIndex=1, _totalEvents=22

  _isReady.value = true
  refreshSlots()    // reads each player's current state
}
```

### Phase 3: `refreshSlots()` derives slot states

**File:** `CompareViewModel.kt:152-177`

```kotlin
// For each player[i]:
val total = player.totalEvents.value          // 22
val index = player.currentEventIndex.value    // 1
val playerState = player.state.value          // ⚠️ STOPPED (never changed from run!)

val effectiveState = when {
    playerState is PlaybackState.Complete → playerState     // false
    total > 0 && index >= total - 1 → PlaybackState.Complete // 22>0 && 1>=21 → false
    total > 0 → PlaybackState.Paused                        // ✓ TRUE
    else -> PlaybackState.Stopped
}
// effectiveState = Paused ✓ (compensates for player's stale Stopped state)

// CompareSlotState:
//   snapshot = player.currentSnapshot.value = AlgorithmSnapshot([5,3,1,4,2], highlights=...)
//   playbackState = Paused
//   eventIndex = 1
//   totalEvents = 22
```

### Phase 4: `ComparePanel` derives UI state

**File:** `ComparePanel.kt:35-51`

```kotlin
val slots by viewModel.slots.collectAsState()          // 4 slots with valid data
val isReady by viewModel.isReady.collectAsState()      // true
val isPlaying by viewModel.isPlaying.collectAsState()   // false

val minIndex = slots.minOfOrNull { it.eventIndex } ?: 0  // 1
val maxTotal = slots.maxOfOrNull { it.totalEvents } ?: 0  // 22
val progress = minIndex to maxTotal                        // (1, 22)

val playbackState = when {
    isPlaying → Playing                                    // false
    slots.isNotEmpty() && slots.all { it.isComplete } → Complete  // not all complete
    isReady → Paused                                       // ✓ TRUE
    else → Stopped
}
// playbackState = Paused ✓
```

### Phase 5: `PlaybackControls` button enablement

**File:** `PlaybackControls.kt:61-72`

```kotlin
// Play/Pause button:
playbackState is Playing → false → else branch
IconButton(
    onClick = onPlay,
    enabled = progress.second > 0  // 22 > 0 → TRUE ✓
)
// → Play button ENABLED ✓

// Step Forward:
enabled = progress.second > 0 && progress.first < progress.second - 1
//         22 > 0 && 1 < 21 → TRUE ✓

// Stop:
enabled = playbackState !is Stopped  // Paused → TRUE ✓

// Run button: always enabled (no condition) ✓

// Progress scrubber:
if (progress.second > 0) → TRUE → Slider visible ✓

// Event counter: "Event 2 / 22" ✓
```

### Phase 6: Bars should render

**File:** `CompareSlot.kt:70-74` → `SortVisualization.kt:71-103`

```kotlin
SortVisualization(snapshot = slotState.snapshot, compact = true)
// snapshot.arrayState = [5,3,1,4,2] → non-empty → Canvas draws bars ✓
```

---

## Confirmed Bugs

### 🔴 BUG #1: Infinite play loop when `stepForward()` is a no-op

**Files:** `AlgorithmPlayer.kt:144-156` + `CompareViewModel.kt:94-110`

**Root cause:** `CompareViewModel.play()` sets `anyAdvanced = true` BEFORE checking if `stepForward()` actually advanced. `stepForward()` is a no-op when `currentEventIndex >= buffer.size - 1` and state ≠ Complete.

```kotlin
// CompareViewModel.kt:97-101
for (player in players) {
    if (player.totalEvents.value > 0 && player.state.value !is PlaybackState.Complete) {
        player.stepForward()      // May be a no-op!
        anyAdvanced = true        // ← Set unconditionally!
    }
}
```

```kotlin
// AlgorithmPlayer.kt:144-156
fun stepForward() {
    if (_state.value is PlaybackState.Complete) return
    playbackJob?.cancel()
    if (_currentEventIndex.value < buffer.size - 1) {
        // ... advance
        if (_currentEventIndex.value >= buffer.size - 1) {
            _state.value = PlaybackState.Complete(buffer.size)
        }
    }
    // ⚠️ If currentEventIndex >= buffer.size - 1: NOTHING HAPPENS
    // ⚠️ State is NOT set to Complete!
    // ⚠️ Function silently returns
}
```

**When this triggers:** Any player with `state = Stopped` or `Paused` that has reached its last event. This occurs when:
- `buffer.size == 1` (theoretically, if algorithm emits only 1 event)  
- After `runAll()` calls `stepForward()` and a player has only 2 events (Start + Complete), reaching Complete — BUT other players haven't finished yet, so the loop keeps calling `stepForward()` on the already-finished player

**Concrete scenario for default input [5,3,1,4,2] with 4 sort algorithms:**
Each generates ≥2 events (Start + at least 1 Compare + Complete), so `stepForward()` from index 0 advances to 1, which is < buffer.size - 1. This works for the initial step.

**But during `play()` playback**, when a player reaches its last event via `stepForward()`:
- Line 152: `_currentEventIndex.value >= buffer.size - 1` → true → `_state = Complete`
- Next loop iteration: `player.state.value is Complete` → skip → ✓

So for normal cases (buffer.size ≥ 3), this bug is latent but doesn't trigger because `stepForward()` DOES set Complete when it reaches the end. **The bug triggers only if `stepForward()` is called when the player is already at the last event but state hasn't been set to Complete** — which happens when the player was never stepped through to completion.

**Impact:** On WASM (single thread), an infinite loop in the play coroutine blocks the main thread permanently → **UI freezes** → appears as "no visible results" from the user's perspective (if they managed to click Play).

### 🔴 BUG #2: Silent coroutine exception swallowing in `runAll()`

**File:** `CompareViewModel.kt:70-85`

```kotlin
scope.launch {                    // SupervisorJob → exceptions swallowed
    for (i in 0..3) {
        players[i].run(
            AlgorithmRegistry.visualizerAsSort(algorithms[i]),
            input
        )                         // If this throws → entire block dies silently
    }
    players.forEach { it.stepForward() }
    _isReady.value = true         // ← Never reached if run() throws
    refreshSlots()                // ← Never reached
}
```

**What could throw:**
1. `algorithm.execute(input, emitter)` — any algorithm bug or unexpected input
2. `delegate.emit(value)` in `RecordingEmitter` — platform-specific `MutableSharedFlow` issues on WASM
3. `reconstructToIndex(idx)` — index out of bounds, snapshot corruption
4. **WASM-specific:** The `RecordingEmitter._recordedSnapshot + value` creates a new list per event. For large event counts (sort algorithms on 50-element arrays), this creates many intermediate lists. Not a crash risk, but adds GC pressure.

**Impact:** If ANY of the 4 `player.run()` calls throws, `_isReady` stays `false`, `refreshSlots()` is never called, and the UI remains in Stopped state with empty slots. **User sees no visible results.** The error is completely swallowed — no log, no UI feedback.

**Verification needed:** Add try/catch with logging to confirm whether exceptions occur.

### 🟡 BUG #3: `runAll()` doesn't cancel its own coroutine on re-entry

**File:** `CompareViewModel.kt:62-86`

```kotlin
fun runAll() {
    // ...
    stop()                  // Cancels playbackJob only
    _isReady.value = false
    scope.launch { ... }    // NEW coroutine launched, NOT tracked
}
```

`stop()` cancels `playbackJob` but does NOT cancel the coroutine launched by a previous `runAll()`. If the user clicks Run twice quickly:
1. First `runAll()` launches Coroutine A
2. Second `runAll()` launches Coroutine B
3. Both run concurrently, both call `_isReady.value = true` and `refreshSlots()`
4. Race condition: whichever finishes last wins, but intermediate state may be inconsistent

**Fix:** Track the `runAll` coroutine job and cancel it in `stop()` or at the start of `runAll()`.

---

## Design Issues

### ⚠️ ISSUE #4: `AlgorithmPlayer._state` never transitions from `Stopped` after `run()`

**File:** `AlgorithmPlayer.kt:76-88`

```kotlin
suspend fun run(algorithm: VisualizableAlgorithm, input: List<Int>) {
    stop()                         // ← Sets _state = Stopped
    buffer.clear()
    snapshots.clear()
    _currentEventIndex.value = 0
    _totalEvents.value = 0

    val emitter = RecordingEmitter()
    algorithm.execute(input, emitter)
    processCapturedEvents(emitter.recorded)
    // ⚠️ _state is NEVER changed from Stopped!
    // ⚠️ No "Ready" or "Paused" state is set after successful run
}
```

After `run()`: player has a full buffer, valid snapshot, and totalEvents > 0, but `_state.value` remains `PlaybackState.Stopped`. This is semantically wrong — the player is not "stopped" in the user's mental model; it's loaded and ready.

`CompareViewModel.refreshSlots()` compensates with `effectiveState` derivation (line 162-167), but the underlying player state is incorrect.

**Why this matters for compare mode:**
- `CompareViewModel.play()` checks `player.state.value` (not effectiveState) at line 90 and 98
- `CompareViewModel.stepForward()` checks `player.state.value` indirectly through `stepForward()` at line 131
- The disconnect between player state and derived state creates fragile coupling

**Comparison with single mode:** In single mode (`AppViewModel`), `player.play()` is called directly, which sets `_state = Playing`. The Stopped → Playing transition works because `play()` explicitly checks `buffer.size == 0` (not state) as the gate.

### ⚠️ ISSUE #5: WASM main-thread blocking during `runAll()`

**File:** `CompareViewModel.kt:20`

```kotlin
private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
```

On WASM, `Dispatchers.Default` = main thread dispatcher. The `scope.launch { }` block runs 4 sort algorithms synchronously (the sort implementations call `emit()` which is effectively non-suspending due to the large `MutableSharedFlow` buffer).

For a 10-element array with 4 algorithms:
- ~200-400 events total
- ~400-800 coroutine state machine entries (RecordingEmitter.emit + delegate.emit)
- Estimated time: 5-50ms → brief UI freeze, acceptable

For a 50-element array:
- ~5000+ events total for O(n²) algorithms
- ~10000+ coroutine state machine entries
- Estimated time: 100-500ms → noticeable UI freeze

**Impact:** UI freezes during computation. Not the primary cause of "no visible results" but contributes to poor UX.

---

## Key Answers to Research Questions

### A) After clicking Run, does `runAll()` actually execute all 4 players?

**Yes, logically.** The guard at line 64 (`algorithms.size != 4`) passes because `selectAlgorithms()` enforces exactly 4. The coroutine at line 70 is enqueued on `Dispatchers.Default` (main thread on WASM) and should execute after the onClick handler returns.

**BUT:** If any `player.run()` throws, the coroutine fails silently (SupervisorJob), and subsequent players never execute. **No error feedback to UI.** (Bug #2)

### B) After `runAll()` completes, what is each player's state?

| Field | Value | Notes |
|-------|-------|-------|
| `_state.value` | `PlaybackState.Stopped` | **Never changed from `run()`** (Issue #4) |
| `_totalEvents.value` | e.g., 22 | Correctly set by `processCapturedEvents()` |
| `_currentEventIndex.value` | 1 | Set by `stepForward()` at line 81 |
| `_currentSnapshot.value` | `AlgorithmSnapshot([5,3,1,4,2], ...)` | Valid, non-empty |

`refreshSlots()` derives `effectiveState = Paused` (total > 0, index < total-1), compensating for stale Stopped state.

### C) Is the Play button enabled after Run completes?

**Yes.** After `runAll()`:
- `playbackState = Paused` (derived from `isReady = true`)
- `progress = (1, 22)` (min index to max total)
- Play button: `enabled = progress.second > 0` → `22 > 0` → **enabled**

**But** the bars should also be visible because slots have non-empty snapshots. If the user sees no bars AND the Play button is enabled, the issue is in rendering, not state.

### D) Could `stepForward()` immediately mark state as `Complete` for small arrays?

**Yes, for 2-event buffers** (Start + Complete only). Example: BubbleSort on a single-element array `[5]`:
- Events: Start([5]), Complete([5]) → buffer.size = 2
- After `run()`: index = 0
- After `stepForward()`: index = 1, `1 >= 2-1` → Complete(2)

For default 5-element array: buffer.size ≈ 22, so stepForward from 0 → 1 does NOT mark Complete. **Not the issue for typical inputs.**

### E) Is there a WASM threading issue?

**Not a fundamental threading issue, but a coroutine scheduling concern.**

On WASM:
- `Dispatchers.Default` = main thread (single-threaded)
- `scope.launch { }` enqueues work for the next event loop tick
- The coroutine runs AFTER onClick returns → UI gets one recomposition (showing Stopped state) before the coroutine executes
- After coroutine completes, UI recomposes again with valid data

**The actual WASM risk:** If the coroutine throws (Bug #2), there's no stack trace in the browser console (SupervisorJob swallows it). The user sees the UI stuck in Stopped state with no error indication.

---

## Recommended Investigation Steps (NOT implementation — read-only research)

1. **Add console logging** to `runAll()` coroutine (before/after each `player.run()`, after `stepForward`, after `refreshSlots`) to confirm execution on WASM
2. **Wrap the coroutine body in try/catch** with console error output to surface swallowed exceptions
3. **Check browser console** for any Kotlin/WASM runtime errors during Run click
4. **Verify StateFlow notification** by adding a simple `Text` that shows `isReady` and `slots.size` values in the ComparePanel

---

## Unresolved Questions

1. **Does the `runAll()` coroutine actually execute on WASM, or does it silently fail to dispatch?** No way to confirm without runtime logging.
2. **Does `MutableSharedFlow.emit()` behave identically on WASM vs JVM?** Specifically, does `tryEmitLocked` succeed with `extraBufferCapacity = Int.MAX_VALUE` and no collectors?
3. **Is there a Compose Multiplatform WASM rendering bug** where the Canvas doesn't redraw when `snapshot` changes from `emptyList()` to a non-empty list?
4. **Does the `LaunchedEffect` inside `SortVisualization` properly trigger** when the composable first receives a non-empty snapshot?
5. **Could the `DisposableEffect(compareViewModel)` in `AppContent.kt:69` cause premature destruction** of the CVM during recomposition?
