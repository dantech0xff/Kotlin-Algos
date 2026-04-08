package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.DescriptionUtils
import com.thealgorithms.core.utils.isLessThan
import com.thealgorithms.core.utils.swapAt
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SortAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class QuickSort : SortAlgorithm {
    override fun <T : Comparable<T>> sort(list: List<T>): List<T> {
        val arr = list.toMutableList()
        doSort(arr, 0, arr.size - 1)
        return arr.toList()
    }

    private fun <T : Comparable<T>> doSort(arr: MutableList<T>, left: Int, right: Int) {
        if (left < right) {
            val pivot = partition(arr, left, right)
            doSort(arr, left, pivot - 1)
            doSort(arr, pivot, right)
        }
    }

    private fun <T : Comparable<T>> partition(arr: MutableList<T>, left: Int, right: Int): Int {
        val mid = (left + right) ushr 1
        val pivot = arr[mid]
        var l = left
        var r = right
        while (l <= r) {
            while (arr[l].isLessThan(pivot)) l++
            while (pivot.isLessThan(arr[r])) r--
            if (l <= r) {
                arr.swapAt(l, r)
                l++; r--
            }
        }
        return l
    }
}

class QuickSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        val arr = input.toMutableList()
        emitter.emit(AlgorithmEvent.Start(input))
        quickSort(arr, 0, arr.size - 1, emitter)
        emitter.emit(AlgorithmEvent.Complete(
            result = arr.toList(),
            description = "Quick sort complete! Array is now sorted."
        ))
    }

    private suspend fun quickSort(
        arr: MutableList<Int>,
        low: Int,
        high: Int,
        emitter: MutableSharedFlow<AlgorithmEvent>,
    ) {
        if (low < high) {
            val pivotIdx = partition(arr, low, high, emitter)
            quickSort(arr, low, pivotIdx - 1, emitter)
            quickSort(arr, pivotIdx + 1, high, emitter)
        }
    }

    private suspend fun partition(
        arr: MutableList<Int>,
        low: Int,
        high: Int,
        emitter: MutableSharedFlow<AlgorithmEvent>,
    ): Int {
        val pivot = arr[high]
        emitter.emit(AlgorithmEvent.Pivot(
            index = high,
            description = DescriptionUtils.pivot(high, arr),
            pseudocodeLine = 3
        ))
        var i = low - 1
        for (j in low until high) {
            emitter.emit(AlgorithmEvent.Compare(
                indices = j to high,
                description = DescriptionUtils.compare(j, high, arr),
                pseudocodeLine = 6
            ))
            if (arr[j] <= pivot) {
                i++
                if (i != j) {
                    val swapDesc = DescriptionUtils.swap(i, j, arr)
                    arr.swapAt(i, j)
                    emitter.emit(AlgorithmEvent.Swap(
                        indices = i to j,
                        description = swapDesc,
                        pseudocodeLine = 8
                    ))
                }
            }
        }
        val pivotSwapDesc = DescriptionUtils.swap(i + 1, high, arr)
        arr.swapAt(i + 1, high)
        emitter.emit(AlgorithmEvent.Swap(
            indices = i + 1 to high,
            description = pivotSwapDesc,
            pseudocodeLine = 9
        ))
        return i + 1
    }
}
