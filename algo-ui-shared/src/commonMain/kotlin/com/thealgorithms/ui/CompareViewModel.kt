package com.thealgorithms.ui

import com.thealgorithms.shared.PlaybackState
import com.thealgorithms.ui.model.AlgorithmInfo
import com.thealgorithms.ui.model.AlgorithmRegistry
import com.thealgorithms.ui.model.CompareSlotState
import com.thealgorithms.viz.AlgorithmPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompareViewModel {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val players = List(4) { AlgorithmPlayer() }
    private var playbackJob: Job? = null

    private val _selectedAlgorithms = MutableStateFlow<List<AlgorithmInfo>>(emptyList())
    val selectedAlgorithms: StateFlow<List<AlgorithmInfo>> = _selectedAlgorithms.asStateFlow()

    private val _inputArray = MutableStateFlow<List<Int>>(listOf(5, 3, 1, 4, 2))
    val inputArray: StateFlow<List<Int>> = _inputArray.asStateFlow()

    private val _speedMs = MutableStateFlow(500L)
    val speedMs: StateFlow<Long> = _speedMs.asStateFlow()

    private val _slots = MutableStateFlow<List<CompareSlotState>>(emptyList())
    val slots: StateFlow<List<CompareSlotState>> = _slots.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    fun selectAlgorithms(algorithms: List<AlgorithmInfo>) {
        require(algorithms.size == 4) { "Must select exactly 4 algorithms" }
        _selectedAlgorithms.value = algorithms
        _isReady.value = false
        refreshSlots()
    }

    fun setInputArray(array: List<Int>) {
        _inputArray.value = array
    }

    fun setSpeed(ms: Long) {
        _speedMs.value = ms.coerceIn(10L, 2000L)
    }

    /**
     * Run all 4 algorithms and immediately play the first step.
     * Uses scope.launch to avoid blocking UI — each player.run() is fast
     * (just populates buffer synchronously, no actual delays).
     */
    fun runAll() {
        val algorithms = _selectedAlgorithms.value
        if (algorithms.size != 4) return
        val input = _inputArray.value

        stop()
        _isReady.value = false

        scope.launch {
            // Run all 4 — each player.run() executes the sort synchronously
            // and populates the event buffer. This is fast (< 100ms total).
            for (i in 0..3) {
                players[i].run(
                    AlgorithmRegistry.visualizerAsSort(algorithms[i]),
                    input
                )
            }

            // Step all to index 0 so bars appear
            players.forEach { it.stepForward() }

            _isReady.value = true
            refreshSlots()
        }
    }

    fun play() {
        if (!_isReady.value) return
        if (players.all { it.state.value is PlaybackState.Complete }) return

        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (true) {
                var anyAdvanced = false
                for (player in players) {
                    if (player.totalEvents.value > 0 && player.state.value !is PlaybackState.Complete) {
                        player.stepForward()
                        anyAdvanced = true
                    }
                }
                refreshSlots()
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
        _isPlaying.value = false
        players.forEach { it.stop() }
        _isReady.value = false
        refreshSlots()
    }

    fun stepForward() {
        if (!_isReady.value) return
        playbackJob?.cancel()
        _isPlaying.value = false
        for (player in players) {
            if (player.totalEvents.value > 0 && player.state.value !is PlaybackState.Complete) {
                player.stepForward()
            }
        }
        refreshSlots()
    }

    fun stepBack() {
        if (!_isReady.value) return
        playbackJob?.cancel()
        _isPlaying.value = false
        players.forEach { it.stepBack() }
        refreshSlots()
    }

    fun destroy() {
        playbackJob?.cancel()
        players.forEach { it.destroy() }
        scope.cancel()
    }

    private fun refreshSlots() {
        val algos = _selectedAlgorithms.value
        if (algos.size != 4) return

        _slots.value = algos.mapIndexed { i, algo ->
            val player = players[i]
            val total = player.totalEvents.value
            val index = player.currentEventIndex.value
            val playerState = player.state.value

            val effectiveState = when {
                playerState is PlaybackState.Complete -> playerState
                total > 0 && index >= total - 1 -> PlaybackState.Complete(total)
                total > 0 -> PlaybackState.Paused
                else -> PlaybackState.Stopped
            }

            CompareSlotState(
                algorithm = algo,
                snapshot = player.currentSnapshot.value,
                playbackState = effectiveState,
                eventIndex = index,
                totalEvents = total,
            )
        }
    }
}
