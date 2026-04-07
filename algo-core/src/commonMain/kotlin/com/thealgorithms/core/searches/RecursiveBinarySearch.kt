package com.thealgorithms.core.searches

import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SearchAlgorithm
import com.thealgorithms.shared.SearchInput
import com.thealgorithms.shared.SearchVisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class RecursiveBinarySearch : SearchAlgorithm {
    override fun <T : Comparable<T>> find(array: List<T>, key: T): Int {
        return search(array, key, 0, array.size - 1)
    }

    private fun <T : Comparable<T>> search(array: List<T>, key: T, left: Int, right: Int): Int {
        if (left > right) return -1
        val mid = (left + right) ushr 1
        return when {
            array[mid] == key -> mid
            array[mid] > key -> search(array, key, left, mid - 1)
            else -> search(array, key, mid + 1, right)
        }
    }
}

class RecursiveBinarySearchVisualizer : SearchVisualizableAlgorithm {
    override suspend fun execute(input: SearchInput, emitter: MutableSharedFlow<AlgorithmEvent>) {
        emitter.emit(AlgorithmEvent.Start(input.array))
        search(input.array, input.key, 0, input.array.size - 1, emitter)
    }

    private suspend fun search(
        array: List<Int>,
        key: Int,
        left: Int,
        right: Int,
        emitter: MutableSharedFlow<AlgorithmEvent>,
    ) {
        if (left > right) {
            emitter.emit(AlgorithmEvent.NotFound)
            return
        }
        val mid = (left + right) ushr 1
        emitter.emit(AlgorithmEvent.RangeCheck(left, right))
        emitter.emit(AlgorithmEvent.Probe(mid))

        when {
            array[mid] == key -> emitter.emit(AlgorithmEvent.Found(mid))
            array[mid] > key -> search(array, key, left, mid - 1, emitter)
            else -> search(array, key, mid + 1, right, emitter)
        }
    }
}
