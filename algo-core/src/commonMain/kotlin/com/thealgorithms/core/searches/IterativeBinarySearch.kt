package com.thealgorithms.core.searches

import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SearchAlgorithm
import com.thealgorithms.shared.SearchInput
import com.thealgorithms.shared.SearchVisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class IterativeBinarySearch : SearchAlgorithm {
    override fun <T : Comparable<T>> find(array: List<T>, key: T): Int {
        var left = 0
        var right = array.size - 1
        while (left <= right) {
            val mid = (left + right) ushr 1
            when {
                array[mid] == key -> return mid
                array[mid] < key -> left = mid + 1
                else -> right = mid - 1
            }
        }
        return -1
    }
}

class IterativeBinarySearchVisualizer : SearchVisualizableAlgorithm {
    override suspend fun execute(input: SearchInput, emitter: MutableSharedFlow<AlgorithmEvent>) {
        emitter.emit(AlgorithmEvent.Start(input.array))

        var left = 0
        var right = input.array.size - 1

        while (left <= right) {
            val mid = (left + right) ushr 1
            emitter.emit(AlgorithmEvent.RangeCheck(left, right))
            emitter.emit(AlgorithmEvent.Probe(mid))

            when {
                input.array[mid] == input.key -> {
                    emitter.emit(AlgorithmEvent.Found(mid))
                    return
                }
                input.array[mid] < input.key -> left = mid + 1
                else -> right = mid - 1
            }
        }

        emitter.emit(AlgorithmEvent.NotFound)
    }
}
