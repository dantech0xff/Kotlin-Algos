package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.isGreaterThan
import com.thealgorithms.core.utils.swapAt
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SortAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class CocktailSort : SortAlgorithm {
    override fun <T : Comparable<T>> sort(list: List<T>): List<T> {
        val arr = list.toMutableList()
        var start = 0
        var end = arr.size - 1
        var swapped = true

        while (swapped) {
            swapped = false

            // Forward pass (like bubble sort)
            for (i in start until end) {
                if (arr[i].isGreaterThan(arr[i + 1])) {
                    arr.swapAt(i, i + 1)
                    swapped = true
                }
            }

            if (!swapped) break

            swapped = false
            end--

            // Backward pass
            for (i in (end - 1) downTo start) {
                if (arr[i].isGreaterThan(arr[i + 1])) {
                    arr.swapAt(i, i + 1)
                    swapped = true
                }
            }

            start++
        }

        return arr.toList()
    }
}

class CocktailSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        val arr = input.toMutableList()
        emitter.emit(AlgorithmEvent.Start(input))

        var start = 0
        var end = arr.size - 1
        var swapped = true

        while (swapped) {
            swapped = false

            // Forward pass
            for (i in start until end) {
                emitter.emit(AlgorithmEvent.Compare(i to i + 1))
                if (arr[i] > arr[i + 1]) {
                    arr.swapAt(i, i + 1)
                    emitter.emit(AlgorithmEvent.Swap(i to i + 1))
                    swapped = true
                }
            }

            if (!swapped) break

            swapped = false
            end--

            // Backward pass
            for (i in (end - 1) downTo start) {
                emitter.emit(AlgorithmEvent.Compare(i to i + 1))
                if (arr[i] > arr[i + 1]) {
                    arr.swapAt(i, i + 1)
                    emitter.emit(AlgorithmEvent.Swap(i to i + 1))
                    swapped = true
                }
            }

            start++
        }

        emitter.emit(AlgorithmEvent.Complete(arr.toList()))
    }
}
