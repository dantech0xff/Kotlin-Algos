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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

private data class PlayerSnapshot(
    val snapshot: AlgorithmSnapshot,
    val eventIndex: Int,
    val totalEvents: Int,
    val state: PlaybackState
)

class CompareViewModel {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val players = List(4) { AlgorithmPlayer() }
    private var runJob: Job? = null
    private var monitorJob: Job? = null

    private val _selectedAlgorithms = MutableStateFlow<List<AlgorithmInfo>>(emptyList())
    val selectedAlgorithms: StateFlow<List<AlgorithmInfo>> = _selectedAlgorithms.asStateFlow()

    private val _inputArray = MutableStateFlow<List<Int>>(listOf(5, 3, 1, 4, 2))
    val inputArray: StateFlow<List<Int>> = _inputArray.asStateFlow()

    private val _speedMs = MutableStateFlow(500L)
    val speedMs: StateFlow<Long> = _speedMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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

    val playbackState: StateFlow<PlaybackState> = combine(
        playerStateFlows[0],
        playerStateFlows[1],
        playerStateFlows[2],
        playerStateFlows[3]
    ) { states ->
        val all = states.map { it.state }
        when {
            all.all { it is PlaybackState.Stopped } -> PlaybackState.Stopped
            all.any { it is PlaybackState.Playing } -> PlaybackState.Playing
            all.all { it is PlaybackState.Complete || it is PlaybackState.Stopped } -> {
                val max = all.filterIsInstance<PlaybackState.Complete>().maxOfOrNull { it.totalEvents } ?: 0
                if (all.any { it is PlaybackState.Complete }) PlaybackState.Complete(max)
                else PlaybackState.Stopped
            }
            else -> PlaybackState.Paused
        }
    }.stateIn(scope, SharingStarted.Lazily, PlaybackState.Stopped)

    fun selectAlgorithms(algorithms: List<AlgorithmInfo>) {
        require(algorithms.size == 4) { "Must select exactly 4 algorithms" }
        _selectedAlgorithms.value = algorithms
        _isReady.value = false
    }

    fun setInputArray(array: List<Int>) {
        _inputArray.value = array
    }

    fun setSpeed(ms: Long) {
        val clamped = ms.coerceIn(10L, 2000L)
        _speedMs.value = clamped
        players.forEach { it.setSpeed(clamped) }
    }

    fun clearError() {
        _error.value = null
    }

    fun runAll() {
        val algorithms = _selectedAlgorithms.value
        if (algorithms.size != 4) {
            _error.value = "Select exactly 4 algorithms"
            return
        }
        val input = _inputArray.value

        stop()
        _error.value = null
        _isReady.value = false

        runJob = scope.launch {
            algorithms.forEachIndexed { i, algoInfo ->
                try {
                    players[i].runAndPrepare(
                        AlgorithmRegistry.visualizerAsSort(algoInfo),
                        input
                    )
                } catch (e: Exception) {
                    _error.value = "Failed to run ${algoInfo.name}: ${e.message}"
                    return@launch
                }
                yield()
            }
            _isReady.value = true
        }
    }

    fun play() {
        if (!_isReady.value) return

        _isPlaying.value = true
        monitorJob?.cancel()
        players.forEach { it.play() }

        monitorJob = scope.launch {
            awaitAll(*players.map { player ->
                async {
                    player.state.first {
                        it is PlaybackState.Complete || it is PlaybackState.Stopped
                    }
                }
            }.toTypedArray())
            _isPlaying.value = false
        }
    }

    fun pause() {
        monitorJob?.cancel()
        players.forEach { it.pause() }
        _isPlaying.value = false
    }

    fun stop() {
        runJob?.cancel()
        monitorJob?.cancel()
        players.forEach { it.stop() }
        _isPlaying.value = false
        _isReady.value = false
    }

    fun stepForward() {
        if (!_isReady.value) return
        players.forEach { it.stepForward() }
    }

    fun stepBack() {
        if (!_isReady.value) return
        players.forEach { it.stepBack() }
    }

    fun destroy() {
        runJob?.cancel()
        monitorJob?.cancel()
        players.forEach { it.destroy() }
        scope.cancel()
    }
}
