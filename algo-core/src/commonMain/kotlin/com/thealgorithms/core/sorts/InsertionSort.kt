package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.DescriptionUtils
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
                emitter.emit(AlgorithmEvent.Compare(
                    indices = j to j + 1,
                    description = DescriptionUtils.compare(j, j + 1, arr),
                    pseudocodeLine = 5
                ))
                if (key < arr[j]) {
                    arr[j + 1] = arr[j]
                    emitter.emit(AlgorithmEvent.Overwrite(
                        index = j + 1,
                        newValue = arr[j],
                        description = DescriptionUtils.overwrite(j + 1, arr[j]),
                        pseudocodeLine = 6
                    ))
                    j--
                } else {
                    break
                }
            }
            arr[j + 1] = key
            emitter.emit(AlgorithmEvent.Overwrite(
                index = j + 1,
                newValue = key,
                description = DescriptionUtils.overwrite(j + 1, key),
                pseudocodeLine = 8
            ))
        }

        emitter.emit(AlgorithmEvent.Complete(
            result = arr.toList(),
            description = "Insertion sort complete! Array is now sorted."
        ))
    }
}
