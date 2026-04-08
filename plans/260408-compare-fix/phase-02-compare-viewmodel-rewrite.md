---
title: "Phase 2: Rewrite CompareViewModel — Reactive Slots"
description: "Replace imperative refreshSlots() with combine()-based reactive slots, delegate playback to players"
status: pending
priority: P1
effort: 2h
phase: 2 of 3
---

# Phase 2: Rewrite CompareViewModel

## Context

Current `CompareViewModel` uses imperative `refreshSlots()` that reads player StateFlows after mutation → stale values on WASM. Playback loop in `play()` calls `stepForward()` manually. Both patterns are fragile and don't leverage Compose's reactive model.

## Overview

- Remove `refreshSlots()` entirely
- Create reactive `slots` StateFlow via `combine()` on all 4 players' flows
- Delegate playback to `player.play()` instead of manual step loop
- Add `yield()` in `runAll()` for WASM recomposition

## File

`algo-ui-shared/src/commonMain/kotlin/com/thealgorithms/ui/CompareViewModel.kt`

## Implementation Steps

### Step 1: Add helper data class and StateFlow combiner

```kotlin
private data class PlayerSnapshot(
    val snapshot: AlgorithmSnapshot,
    val eventIndex: Int,
    val totalEvents: Int,
    val state: PlaybackState
)

private fun playerStateFlow(player: AlgorithmPlayer): StateFlow<PlayerSnapshot> =
    combine(
        player.currentSnapshot,
        player.currentEventIndex,
        player.totalEvents,
        player.state
    ) { snap, idx, total, st ->
        PlayerSnapshot(snap, idx, total, st)
    }.stateIn(
        scope,
        SharingStarted.Lazily,
        PlayerSnapshot(AlgorithmSnapshot(emptyList()), 0, 0, PlaybackState.Stopped)
    )
```

- Place as private members in class body
- `playerStateFlows` list initialized in `init` or lazily

### Step 2: Replace `slots` with reactive `combine()`

```kotlin
private val playerStateFlows: List<StateFlow<PlayerSnapshot>> =
    players.map { playerStateFlow(it) }

val slots: StateFlow<List<CompareSlotState>> = combine(
    _selectedAlgorithms,
    playerStateFlows[0],
    playerStateFlows[1],
    playerStateFlows[2],
    playerStateFlows[3]
) { values ->
    val algos = values[0] as List<AlgorithmInfo>
    if (algos.size != 4) emptyList()
    else algos.mapIndexed { i, algo ->
        val ps = values[i + 1] as PlayerSnapshot
        CompareSlotState(
            algorithm = algo,
            snapshot = ps.snapshot,
            playbackState = ps.state,
            eventIndex = ps.eventIndex,
            totalEvents = ps.totalEvents,
        )
    }
}.stateIn(scope, SharingStarted.Lazily, emptyList())
```

- Remove old `_slots` MutableStateFlow and `refreshSlots()` method
- `slots` is now derived reactively — always up-to-date
- Compose collects this → automatic recomposition

### Step 3: Rewrite `runAll()`

```kotlin
fun runAll() {
    scope.launch {
        _isReady.value = false
        val selected = _selectedAlgorithms.value
        if (selected.size != 4) return@launch

        selected.forEachIndexed { i, algoInfo ->
            val player = players[i]
            val algorithm = algorithmFactory.create(algoInfo.id)
            val input = inputGenerator.generate(algoInfo.id)
            player.runAndPrepare(algorithm, input)
            yield()  // Allow WASM to recompose between players
        }
        _isReady.value = true
    }
}
```

- Uses `runAndPrepare()` from Phase 1
- `yield()` after each player → WASM can update UI between runs
- Sets `_isReady = true` when all done

### Step 4: Rewrite `play()` — delegate to `player.play()`

```kotlin
fun play() {
    if (!_isReady.value) return
    _isPlaying.value = true
    players.forEach { it.play() }
    // Monitor completion
    scope.launch {
        players.map { it.state }.merge()
            .first { states -> states.all { it == PlaybackState.Complete || it == PlaybackState.Stopped } }
        _isPlaying.value = false
    }
}
```

Alternative (simpler, more robust):
```kotlin
fun play() {
    if (!_isReady.value) return
    _isPlaying.value = true
    players.forEach { it.play() }
    scope.launch {
        // Wait for all to complete
        players.forEach { player ->
            player.state.first { it == PlaybackState.Complete || it == PlaybackState.Stopped }
        }
        _isPlaying.value = false
    }
}
```

Wait — `forEach` with `first` is sequential. Better approach:

```kotlin
fun play() {
    if (!_isReady.value) return
    _isPlaying.value = true
    players.forEach { it.play() }
    scope.launch {
        awaitAll(*players.map { p ->
            async { p.state.first { it == PlaybackState.Complete || it == PlaybackState.Stopped } }
        }.toTypedArray())
        _isPlaying.value = false
    }
}
```

Pick the `awaitAll` approach — parallel monitoring, all must complete.

### Step 5: Rewrite `pause()`, `stop()`, `stepForward()`, `stepBack()`, `setSpeed()`

```kotlin
fun pause() {
    players.forEach { it.pause() }
    _isPlaying.value = false
}

fun stop() {
    players.forEach { it.stop() }
    _isPlaying.value = false
    _isReady.value = false
}

fun stepForward() {
    players.forEach { it.stepForward() }
}

fun stepBack() {
    players.forEach { it.stepBackward() }
}

fun setSpeed(speed: Float) {
    players.forEach { it.setSpeed(speed) }
}
```

- One-liners — delegate to players
- State updates happen automatically via reactive `slots`
- No manual `refreshSlots()` needed

### Step 6: Remove dead code

- Remove `refreshSlots()` method
- Remove `_slots` MutableStateFlow
- Remove `effectiveState` computation (if exists as standalone)
- Remove any manual `_slots.value = ...` assignments

### Step 7: Clean up playback state derivation

Replace any manual playback state tracking with:

```kotlin
val playbackState: StateFlow<PlaybackState> = combine(
    players.map { it.state }
) { states ->
    when {
        states.all { it == PlaybackState.Stopped } -> PlaybackState.Stopped
        states.any { it == PlaybackState.Playing } -> PlaybackState.Playing
        states.all { it == PlaybackState.Complete } -> PlaybackState.Complete
        else -> PlaybackState.Paused
    }
}.stateIn(scope, SharingStarted.Lazily, PlaybackState.Stopped)
```

- Derives aggregate state from 4 players
- ComparePanel can collect this directly

## Success Criteria

- [ ] `slots` StateFlow updates reactively when any player state changes
- [ ] No `refreshSlots()` call anywhere in codebase
- [ ] `runAll()` yields between players (WASM-friendly)
- [ ] `play()` delegates to `player.play()` for all 4 players
- [ ] `_isPlaying` becomes `false` when all players reach Complete
- [ ] Pause/Stop immediately halt all 4 players
- [ ] Step controls affect all 4 players simultaneously

## Risk Assessment

| Risk | Likelihood | Mitigation |
|------|-----------|------------|
| `combine()` with 5 flows complexity | Low | Kotlin coroutines handles this natively |
| Players out of sync | Medium | All share same speed, start together — drift minimal |
| Memory from 4 combined flows | Low | StateFlows are lightweight, shared already |
| `awaitAll` cancellation behavior | Low | Scope is ViewModel-scoped, cancels with VM |
