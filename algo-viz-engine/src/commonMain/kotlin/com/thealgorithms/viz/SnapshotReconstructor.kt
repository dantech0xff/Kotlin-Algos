package com.thealgorithms.viz

import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.HighlightReason

class SnapshotReconstructor {
    fun reconstruct(snapshot: AlgorithmSnapshot, events: List<AlgorithmEvent>): AlgorithmSnapshot {
        var state = snapshot.arrayState.toMutableList()
        var comparisons = snapshot.comparisons
        var swaps = snapshot.swaps
        var highlights = snapshot.highlights.toMutableMap()
        var sortedIndices = snapshot.sortedIndices.toMutableSet()

        for (event in events) {
            highlights.clear()
            when (event) {
                is AlgorithmEvent.Swap -> {
                    val (i, j) = event.indices
                    val temp = state[i]
                    state[i] = state[j]
                    state[j] = temp
                    swaps++
                    highlights[i] = HighlightReason.SWAPPING
                    highlights[j] = HighlightReason.SWAPPING
                }
                is AlgorithmEvent.Overwrite -> {
                    state[event.index] = event.newValue
                    highlights[event.index] = HighlightReason.OVERWRITING
                }
                is AlgorithmEvent.Compare -> {
                    comparisons++
                    highlights[event.indices.first] = HighlightReason.COMPARING
                    highlights[event.indices.second] = HighlightReason.COMPARING
                }
                is AlgorithmEvent.Select -> highlights[event.index] = HighlightReason.SELECTING
                is AlgorithmEvent.Deselect -> highlights.clear()
                is AlgorithmEvent.Pivot -> highlights[event.index] = HighlightReason.PIVOTING
                is AlgorithmEvent.Probe -> highlights[event.index] = HighlightReason.PROBING
                is AlgorithmEvent.Found -> highlights[event.index] = HighlightReason.FOUND
                is AlgorithmEvent.RangeCheck -> {
                    for (idx in event.low..event.high) {
                        highlights[idx] = HighlightReason.RANGE
                    }
                }
                is AlgorithmEvent.Start -> {
                    state = event.data.toMutableList()
                    highlights.clear()
                    sortedIndices.clear()
                }
                is AlgorithmEvent.Complete -> {
                    state = event.result.toMutableList()
                    highlights.clear()
                    sortedIndices = state.indices.toMutableSet()
                }
                is AlgorithmEvent.NotFound -> highlights.clear()
            }
        }

        return AlgorithmSnapshot(state.toList(), highlights.toMap(), comparisons, swaps, sortedIndices)
    }
}
