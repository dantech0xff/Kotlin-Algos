package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.DescriptionUtils
import com.thealgorithms.core.utils.isGreaterThan
import com.thealgorithms.core.utils.swapAt
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SortAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class BubbleSort : SortAlgorithm {
    override fun <T : Comparable<T>> sort(list: List<T>): List<T> {
        val arr = list.toMutableList()
        for (i in 1 until arr.size) {
            var swapped = false
            for (j in 0 until arr.size - i) {
                if (arr[j].isGreaterThan(arr[j + 1])) {
                    arr.swapAt(j, j + 1)
                    swapped = true
                }
            }
            if (!swapped) break
        }
        return arr.toList()
    }
}

class BubbleSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        val arr = input.toMutableList()
        emitter.emit(AlgorithmEvent.Start(input))

        for (i in 1 until arr.size) {
            var swapped = false
            for (j in 0 until arr.size - i) {
                emitter.emit(AlgorithmEvent.Compare(
                    indices = j to j + 1,
                    description = DescriptionUtils.compare(j, j + 1, arr),
                    pseudocodeLine = 4
                ))
                if (arr[j] > arr[j + 1]) {
                    val swapDesc = DescriptionUtils.swap(j, j + 1, arr)
                    arr.swapAt(j, j + 1)
                    emitter.emit(AlgorithmEvent.Swap(
                        indices = j to j + 1,
                        description = swapDesc,
                        pseudocodeLine = 5
                    ))
                    swapped = true
                }
            }
            if (!swapped) break
        }

        emitter.emit(AlgorithmEvent.Complete(
            result = arr.toList(),
            description = "Bubble sort complete! Array is now sorted."
        ))
    }
}
