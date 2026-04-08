package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.DescriptionUtils
import com.thealgorithms.core.utils.isLessThan
import com.thealgorithms.core.utils.swapAt
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SortAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class SelectionSort : SortAlgorithm {
    override fun <T : Comparable<T>> sort(list: List<T>): List<T> {
        val arr = list.toMutableList()
        for (i in 0 until arr.size - 1) {
            val minIndex = findIndexOfMin(arr, i)
            arr.swapAt(i, minIndex)
        }
        return arr.toList()
    }

    private companion object {
        fun <T : Comparable<T>> findIndexOfMin(arr: MutableList<T>, startIndex: Int): Int {
            var minIndex = startIndex
            for (i in startIndex + 1 until arr.size) {
                if (arr[i].isLessThan(arr[minIndex])) {
                    minIndex = i
                }
            }
            return minIndex
        }
    }
}

class SelectionSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        val arr = input.toMutableList()
        emitter.emit(AlgorithmEvent.Start(input))

        for (i in 0 until arr.size - 1) {
            var minIndex = i
            emitter.emit(AlgorithmEvent.Select(i))
            for (j in i + 1 until arr.size) {
                emitter.emit(AlgorithmEvent.Compare(
                    indices = j to minIndex,
                    description = DescriptionUtils.compare(j, minIndex, arr),
                    pseudocodeLine = 5
                ))
                if (arr[j] < arr[minIndex]) {
                    emitter.emit(AlgorithmEvent.Deselect(minIndex))
                    minIndex = j
                    emitter.emit(AlgorithmEvent.Select(minIndex))
                }
            }
            if (i != minIndex) {
                val swapDesc = DescriptionUtils.swap(i, minIndex, arr)
                arr.swapAt(i, minIndex)
                emitter.emit(AlgorithmEvent.Swap(
                    indices = i to minIndex,
                    description = swapDesc,
                    pseudocodeLine = 7
                ))
            }
            emitter.emit(AlgorithmEvent.Deselect(minIndex))
        }

        emitter.emit(AlgorithmEvent.Complete(
            result = arr.toList(),
            description = "Selection sort complete! Array is now sorted."
        ))
    }
}
