package com.thealgorithms.shared

sealed interface PlaybackState {
    data object Stopped : PlaybackState
    data object Playing : PlaybackState
    data object Paused : PlaybackState
    data class Complete(val totalEvents: Int) : PlaybackState
}
