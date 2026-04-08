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
        var description = snapshot.currentDescription
        var pseudocodeLine = snapshot.activePseudocodeLine

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
                    description = event.description
                    pseudocodeLine = event.pseudocodeLine
                }
                is AlgorithmEvent.Overwrite -> {
                    state[event.index] = event.newValue
                    highlights[event.index] = HighlightReason.OVERWRITING
                    description = event.description
                    pseudocodeLine = event.pseudocodeLine
                }
                is AlgorithmEvent.Compare -> {
                    comparisons++
                    highlights[event.indices.first] = HighlightReason.COMPARING
                    highlights[event.indices.second] = HighlightReason.COMPARING
                    description = event.description
                    pseudocodeLine = event.pseudocodeLine
                }
                is AlgorithmEvent.Select -> highlights[event.index] = HighlightReason.SELECTING
                is AlgorithmEvent.Deselect -> highlights.clear()
                is AlgorithmEvent.Pivot -> {
                    highlights[event.index] = HighlightReason.PIVOTING
                    description = event.description
                    pseudocodeLine = event.pseudocodeLine
                }
                is AlgorithmEvent.Probe -> {
                    highlights[event.index] = HighlightReason.PROBING
                    description = event.description
                    pseudocodeLine = event.pseudocodeLine
                }
                is AlgorithmEvent.Found -> {
                    highlights[event.index] = HighlightReason.FOUND
                    description = event.description
                    pseudocodeLine = event.pseudocodeLine
                }
                is AlgorithmEvent.RangeCheck -> {
                    for (idx in event.low..event.high) {
                        highlights[idx] = HighlightReason.RANGE
                    }
                    description = event.description
                    pseudocodeLine = event.pseudocodeLine
                }
                is AlgorithmEvent.Start -> {
                    state = event.data.toMutableList()
                    highlights.clear()
                    sortedIndices.clear()
                    description = ""
                    pseudocodeLine = null
                }
                is AlgorithmEvent.Complete -> {
                    state = event.result.toMutableList()
                    highlights.clear()
                    sortedIndices = state.indices.toMutableSet()
                    description = event.description
                    pseudocodeLine = null
                }
                is AlgorithmEvent.NotFound -> {
                    highlights.clear()
                    description = ""
                    pseudocodeLine = null
                }
            }
        }

        return AlgorithmSnapshot(state.toList(), highlights.toMap(), comparisons, swaps, sortedIndices, description, pseudocodeLine)
    }
}
