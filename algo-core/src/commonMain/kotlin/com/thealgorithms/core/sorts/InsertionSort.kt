package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.isLessThan
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SortAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class InsertionSort : SortAlgorithm {
    override fun <T : Comparable<T>> sort(list: List<T>): List<T> {
        val arr = list.toMutableList()
        for (i in 1 until arr.size) {
            val key = arr[i]
            var j = i - 1
            while (j >= 0 && key.isLessThan(arr[j])) {
                arr[j + 1] = arr[j]
                j--
            }
            arr[j + 1] = key
        }
        return arr.toList()
    }
}

class InsertionSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        val arr = input.toMutableList()
        emitter.emit(AlgorithmEvent.Start(input))

        for (i in 1 until arr.size) {
            val key = arr[i]
            var j = i - 1
            while (j >= 0) {
                emitter.emit(AlgorithmEvent.Compare(j to j + 1))
                if (key < arr[j]) {
                    arr[j + 1] = arr[j]
                    emitter.emit(AlgorithmEvent.Overwrite(j + 1, arr[j]))
                    j--
                } else {
                    break
                }
            }
            arr[j + 1] = key
            emitter.emit(AlgorithmEvent.Overwrite(j + 1, key))
        }

        emitter.emit(AlgorithmEvent.Complete(arr.toList()))
    }
}
