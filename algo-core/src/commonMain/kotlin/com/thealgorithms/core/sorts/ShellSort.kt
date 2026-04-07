package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.isGreaterThan
import com.thealgorithms.core.utils.swapAt
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SortAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class ShellSort : SortAlgorithm {
    override fun <T : Comparable<T>> sort(list: List<T>): List<T> {
        val arr = list.toMutableList()
        var gap = arr.size / 2

        while (gap > 0) {
            for (i in gap until arr.size) {
                var j = i
                while (j >= gap && arr[j - gap].isGreaterThan(arr[j])) {
                    arr.swapAt(j, j - gap)
                    j -= gap
                }
            }
            gap /= 2
        }

        return arr.toList()
    }
}

class ShellSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        val arr = input.toMutableList()
        emitter.emit(AlgorithmEvent.Start(input))

        var gap = arr.size / 2

        while (gap > 0) {
            for (i in gap until arr.size) {
                var j = i
                while (j >= gap) {
                    emitter.emit(AlgorithmEvent.Compare(j - gap to j))
                    if (arr[j - gap] > arr[j]) {
                        emitter.emit(AlgorithmEvent.Swap(j - gap to j))
                        arr.swapAt(j, j - gap)
                        j -= gap
                    } else {
                        break
                    }
                }
            }
            gap /= 2
        }

        emitter.emit(AlgorithmEvent.Complete(arr.toList()))
    }
}
