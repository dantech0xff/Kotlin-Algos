package com.thealgorithms.viz

import com.thealgorithms.shared.AlgorithmEvent

class SnapshotReconstructor {
    fun reconstruct(snapshot: AlgorithmSnapshot, events: List<AlgorithmEvent>): AlgorithmSnapshot {
        var state = snapshot.arrayState.toMutableList()
        var comparisons = snapshot.comparisons
        var swaps = snapshot.swaps
        var highlighted = snapshot.highlightedIndices

        for (event in events) {
            when (event) {
                is AlgorithmEvent.Swap -> {
                    val (i, j) = event.indices
                    val temp = state[i]
                    state[i] = state[j]
                    state[j] = temp
                    swaps++
                    highlighted = setOf(i, j)
                }
                is AlgorithmEvent.Overwrite -> {
                    state[event.index] = event.newValue
                    highlighted = setOf(event.index)
                }
                is AlgorithmEvent.Compare -> {
                    comparisons++
                    highlighted = setOf(event.indices.first, event.indices.second)
                }
                is AlgorithmEvent.Select -> highlighted = setOf(event.index)
                is AlgorithmEvent.Deselect -> highlighted = emptySet()
                is AlgorithmEvent.Pivot -> highlighted = setOf(event.index)
                is AlgorithmEvent.Probe -> highlighted = setOf(event.index)
                is AlgorithmEvent.Found -> highlighted = setOf(event.index)
                is AlgorithmEvent.RangeCheck -> highlighted = (event.low..event.high).toSet()
                is AlgorithmEvent.Start -> {
                    state = event.data.toMutableList()
                    highlighted = emptySet()
                }
                is AlgorithmEvent.Complete -> {
                    state = event.result.toMutableList()
                    highlighted = emptySet()
                }
                is AlgorithmEvent.NotFound -> highlighted = emptySet()
            }
        }

        return AlgorithmSnapshot(state.toList(), highlighted, comparisons, swaps)
    }
}
