package com.thealgorithms.core.searches

import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SearchAlgorithm
import com.thealgorithms.shared.SearchInput
import com.thealgorithms.shared.SearchVisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class LinearSearch : SearchAlgorithm {
    override fun <T : Comparable<T>> find(array: List<T>, key: T): Int {
        for (i in array.indices) {
            if (array[i] == key) return i
        }
        return -1
    }
}

class LinearSearchVisualizer : SearchVisualizableAlgorithm {
    override suspend fun execute(input: SearchInput, emitter: MutableSharedFlow<AlgorithmEvent>) {
        emitter.emit(AlgorithmEvent.Start(input.array))

        for (i in input.array.indices) {
            emitter.emit(AlgorithmEvent.Probe(i))
            if (input.array[i] == input.key) {
                emitter.emit(AlgorithmEvent.Found(i))
                return
            }
        }

        emitter.emit(AlgorithmEvent.NotFound)
    }
}
