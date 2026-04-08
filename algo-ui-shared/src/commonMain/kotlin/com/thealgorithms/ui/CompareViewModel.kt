package com.thealgorithms.ui

import com.thealgorithms.shared.PlaybackState
import com.thealgorithms.ui.model.AlgorithmInfo
import com.thealgorithms.ui.model.AlgorithmRegistry
import com.thealgorithms.ui.model.CompareSlotState
import com.thealgorithms.viz.AlgorithmPlayer
import com.thealgorithms.viz.AlgorithmSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
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

    val isPlaying: StateFlow<Boolean> get() = _isPlaying
    private val _isPlaying = MutableStateFlow(false)

    fun selectAlgorithms(algorithms: List<AlgorithmInfo>) {
        require(algorithms.size == 4) { "Must select exactly 4 algorithms" }
        _selectedAlgorithms.value = algorithms
        updateSlots()
    }

    fun setInputArray(array: List<Int>) {
        _inputArray.value = array
    }

    fun setSpeed(ms: Long) {
        _speedMs.value = ms.coerceIn(10L, 2000L)
    }

    fun runAll() {
        val algorithms = _selectedAlgorithms.value
        if (algorithms.size != 4) return
        val input = _inputArray.value

        playbackJob?.cancel()
        _isPlaying.value = false

        scope.launch {
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

    fun play() {
        if (_slots.value.isEmpty()) return
        if (_slots.value.all { it.isComplete }) return

        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (true) {
                var anyAdvanced = false
                players.forEachIndexed { i, player ->
                    if (player.state.value !is PlaybackState.Complete) {
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
        _isPlaying.value = false
        players.forEach { it.stop() }
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

    fun destroy() {
        playbackJob?.cancel()
        players.forEach { it.destroy() }
        scope.cancel()
    }

    private fun updateSlots() {
        val algos = _selectedAlgorithms.value
        if (algos.size != 4) return

        _slots.value = algos.mapIndexed { i, algo ->
            CompareSlotState(
                algorithm = algo,
                snapshot = players[i].currentSnapshot.value,
                playbackState = players[i].state.value,
                eventIndex = players[i].currentEventIndex.value,
                totalEvents = players[i].totalEvents.value,
            )
        }
    }
}
