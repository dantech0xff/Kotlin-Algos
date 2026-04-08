package com.thealgorithms.ui.model

import com.thealgorithms.shared.PlaybackState
import com.thealgorithms.viz.AlgorithmSnapshot

data class CompareSlotState(
    val algorithm: AlgorithmInfo,
    val snapshot: AlgorithmSnapshot = AlgorithmSnapshot(emptyList()),
    val playbackState: PlaybackState = PlaybackState.Stopped,
    val eventIndex: Int = 0,
    val totalEvents: Int = 0,
) {
    val comparisons: Int get() = snapshot.comparisons
    val swaps: Int get() = snapshot.swaps
    val isComplete: Boolean get() = playbackState is PlaybackState.Complete
}
