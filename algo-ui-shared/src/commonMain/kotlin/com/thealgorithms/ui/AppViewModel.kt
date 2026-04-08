package com.thealgorithms.ui

import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.PlaybackState
import com.thealgorithms.shared.SearchInput
import com.thealgorithms.shared.SearchVisualizableAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm
import com.thealgorithms.ui.model.AlgorithmCategory
import com.thealgorithms.ui.model.AlgorithmInfo
import com.thealgorithms.ui.model.AlgorithmRegistry
import com.thealgorithms.ui.model.VizKey
import com.thealgorithms.viz.AlgorithmPlayer
import com.thealgorithms.viz.AlgorithmSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel {
    private val player = AlgorithmPlayer()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val playbackState: StateFlow<PlaybackState> = player.state
    val snapshot: StateFlow<AlgorithmSnapshot> = player.currentSnapshot
    val eventIndex: StateFlow<Int> = player.currentEventIndex
    val totalEvents: StateFlow<Int> = player.totalEvents

    private val _selectedAlgorithm = MutableStateFlow<AlgorithmInfo?>(null)
    val selectedAlgorithm: StateFlow<AlgorithmInfo?> = _selectedAlgorithm.asStateFlow()

    private val _inputArray = MutableStateFlow<List<Int>>(listOf(5, 3, 1, 4, 2))
    val inputArray: StateFlow<List<Int>> = _inputArray.asStateFlow()

    private val _searchKey = MutableStateFlow(4)
    val searchKey: StateFlow<Int> = _searchKey.asStateFlow()

    private val _speedMs = MutableStateFlow(500L)
    val speedMs: StateFlow<Long> = _speedMs.asStateFlow()

    val progress: StateFlow<Pair<Int, Int>> = combine(eventIndex, totalEvents) { idx, total ->
        idx to total
    }.stateIn(scope, SharingStarted.Lazily, 0 to 0)

    fun selectAlgorithm(info: AlgorithmInfo) {
        _selectedAlgorithm.value = info
    }

    fun setInputArray(array: List<Int>) {
        _inputArray.value = array
    }

    fun setSearchKey(key: Int) {
        _searchKey.value = key
    }

    fun setSpeed(ms: Long) {
        val clamped = ms.coerceIn(10L, 2000L)
        _speedMs.value = clamped
        player.setSpeed(clamped)
    }

    fun runAlgorithm() {
        val algo = _selectedAlgorithm.value ?: return
        scope.launch {
            when (algo.category) {
                AlgorithmCategory.SORTING -> {
                    player.run(
                        AlgorithmRegistry.visualizerAsSort(algo),
                        _inputArray.value
                    )
                }
                AlgorithmCategory.SEARCHING -> {
                    val searchInput = SearchInput(_inputArray.value, _searchKey.value)
                    val searchVis = AlgorithmRegistry.visualizerAsSearch(algo)
                    val adapter = SearchAlgorithmAdapter(searchVis, searchInput)
                    player.run(adapter, _inputArray.value)
                }
            }
        }
    }

    fun play() = player.play()
    fun pause() = player.pause()
    fun stop() = player.stop()
    fun stepForward() = player.stepForward()
    fun stepBack() = player.stepBack()
    fun seekTo(index: Int) = player.seekTo(index)

    fun onKey(key: VizKey) {
        when (key) {
            VizKey.PLAY_PAUSE -> {
                val state = playbackState.value
                if (state is PlaybackState.Playing) pause() else play()
            }
            VizKey.STEP_FORWARD -> stepForward()
            VizKey.STEP_BACK -> stepBack()
            VizKey.RESET -> stop()
        }
    }

    fun destroy() = player.destroy()
}

private class SearchAlgorithmAdapter(
    private val searchAlgo: SearchVisualizableAlgorithm,
    private val searchInput: SearchInput
) : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        searchAlgo.execute(searchInput, emitter)
    }
}
