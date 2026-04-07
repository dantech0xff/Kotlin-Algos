package com.thealgorithms.viz

import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.PlaybackState
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow

/**
 * A [MutableSharedFlow] wrapper that records all emitted events into a
 * thread-safe list, allowing synchronous access to the event stream
 * without requiring a separate collector coroutine.
 */
internal class RecordingEmitter(
    private val delegate: MutableSharedFlow<AlgorithmEvent> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
) : MutableSharedFlow<AlgorithmEvent> by delegate {

    @Volatile
    private var _recordedSnapshot: List<AlgorithmEvent> = emptyList()

    val recorded: List<AlgorithmEvent> get() = _recordedSnapshot

    override suspend fun emit(value: AlgorithmEvent) {
        _recordedSnapshot = _recordedSnapshot + value
        delegate.emit(value)
    }
}

class AlgorithmPlayer(
    private val snapshotInterval: Int = 10,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val buffer = EventBuffer()
    private val reconstructor = SnapshotReconstructor()
    private val snapshots = mutableMapOf<Int, AlgorithmSnapshot>()

    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Stopped)
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val _currentSnapshot = MutableStateFlow(AlgorithmSnapshot(emptyList()))
    val currentSnapshot: StateFlow<AlgorithmSnapshot> = _currentSnapshot.asStateFlow()

    private val _currentEventIndex = MutableStateFlow(0)
    val currentEventIndex: StateFlow<Int> = _currentEventIndex.asStateFlow()

    private val _totalEvents = MutableStateFlow(0)
    val totalEvents: StateFlow<Int> = _totalEvents.asStateFlow()

    private var playbackJob: Job? = null
    private var _speedMs = MutableStateFlow(500L)

    fun setSpeed(msPerEvent: Long) {
        require(msPerEvent in 10..2000) { "Speed must be between 10ms and 2000ms" }
        _speedMs.value = msPerEvent
    }

    /**
     * Runs the algorithm, collecting all emitted events into the internal buffer.
     *
     * Uses a [RecordingEmitter] that captures events synchronously as they are
     * emitted, avoiding the need for a separate collector coroutine. This works
     * correctly under both real and virtual (test) coroutine dispatchers.
     */
    suspend fun run(algorithm: VisualizableAlgorithm, input: List<Int>) {
        stop()
        buffer.clear()
        snapshots.clear()
        _currentEventIndex.value = 0
        _totalEvents.value = 0

        val emitter = RecordingEmitter()

        algorithm.execute(input, emitter)

        processCapturedEvents(emitter.recorded)
    }

    private fun processCapturedEvents(events: List<AlgorithmEvent>) {
        for (event in events) {
            buffer.add(event)
            val idx = buffer.size - 1
            if (idx % snapshotInterval == 0) {
                snapshots[idx] = reconstructToIndex(idx)
            }
        }

        _totalEvents.value = buffer.size

        if (buffer.size > 0) {
            _currentSnapshot.value = reconstructToIndex(0)
        }
    }

    fun play() {
        if (_state.value is PlaybackState.Complete) return
        if (buffer.size == 0) return

        _state.value = PlaybackState.Playing

        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (_currentEventIndex.value < buffer.size - 1) {
                _currentEventIndex.value++
                _currentSnapshot.value = reconstructToIndex(_currentEventIndex.value)

                if (_currentEventIndex.value >= buffer.size - 1) {
                    _state.value = PlaybackState.Complete(buffer.size)
                    break
                }

                delay(_speedMs.value)
            }
        }
    }

    fun pause() {
        playbackJob?.cancel()
        if (_state.value is PlaybackState.Playing) {
            _state.value = PlaybackState.Paused
        }
    }

    fun stop() {
        playbackJob?.cancel()
        _state.value = PlaybackState.Stopped
        _currentEventIndex.value = 0
        if (buffer.size > 0) {
            _currentSnapshot.value = reconstructToIndex(0)
        }
    }

    fun stepForward() {
        if (_state.value is PlaybackState.Complete) return
        playbackJob?.cancel()

        if (_currentEventIndex.value < buffer.size - 1) {
            _currentEventIndex.value++
            _currentSnapshot.value = reconstructToIndex(_currentEventIndex.value)

            if (_currentEventIndex.value >= buffer.size - 1) {
                _state.value = PlaybackState.Complete(buffer.size)
            }
        }
    }

    fun stepBack() {
        playbackJob?.cancel()

        if (_currentEventIndex.value > 0) {
            _currentEventIndex.value--
            _currentSnapshot.value = reconstructToIndex(_currentEventIndex.value)
            _state.value = PlaybackState.Paused
        }
    }

    private fun reconstructToIndex(index: Int): AlgorithmSnapshot {
        if (buffer.size == 0) return AlgorithmSnapshot(emptyList())

        val snapshotIndex = snapshots.keys.filter { it <= index }.maxOrNull()

        val baseSnapshot: AlgorithmSnapshot
        val startIndex: Int

        if (snapshotIndex != null) {
            baseSnapshot = snapshots[snapshotIndex]!!
            startIndex = snapshotIndex
        } else {
            baseSnapshot = AlgorithmSnapshot(emptyList())
            startIndex = -1 // No snapshot; apply events from 0
        }

        // Apply events from (startIndex+1) through index inclusive
        val deltaEvents = if (startIndex < index) {
            ((startIndex + 1)..index).map { buffer.get(it) }
        } else {
            emptyList()
        }

        return if (deltaEvents.isEmpty()) baseSnapshot
        else reconstructor.reconstruct(baseSnapshot, deltaEvents)
    }

    fun destroy() {
        playbackJob?.cancel()
        scope.cancel()
    }
}
