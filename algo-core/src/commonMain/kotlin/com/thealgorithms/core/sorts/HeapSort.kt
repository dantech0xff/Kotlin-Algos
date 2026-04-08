package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.DescriptionUtils
import com.thealgorithms.core.utils.isGreaterThan
import com.thealgorithms.core.utils.swapAt
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SortAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class HeapSort : SortAlgorithm {
    override fun <T : Comparable<T>> sort(list: List<T>): List<T> {
        val arr = list.toMutableList()
        val n = arr.size

        // Build max heap
        for (i in (n / 2 - 1) downTo 0) {
            heapify(arr, n, i)
        }

        // Extract elements from heap one by one
        for (i in (n - 1) downTo 1) {
            arr.swapAt(0, i)
            heapify(arr, i, 0)
        }

        return arr.toList()
    }

    private fun <T : Comparable<T>> heapify(arr: MutableList<T>, n: Int, i: Int) {
        var largest = i
        val left = 2 * i + 1
        val right = 2 * i + 2

        if (left < n && arr[left].isGreaterThan(arr[largest])) largest = left
        if (right < n && arr[right].isGreaterThan(arr[largest])) largest = right

        if (largest != i) {
            arr.swapAt(i, largest)
            heapify(arr, n, largest)
        }
    }
}

class HeapSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        val arr = input.toMutableList()
        val n = arr.size
        emitter.emit(AlgorithmEvent.Start(input))

        // Build max heap
        for (i in (n / 2 - 1) downTo 0) {
            heapify(arr, n, i, emitter)
        }

        // Extract elements from heap one by one
        for (i in (n - 1) downTo 1) {
            emitter.emit(AlgorithmEvent.Swap(
                indices = 0 to i,
                description = DescriptionUtils.swap(0, i, arr),
                pseudocodeLine = 4
            ))
            arr.swapAt(0, i)
            heapify(arr, i, 0, emitter)
        }

        emitter.emit(AlgorithmEvent.Complete(
            result = arr.toList(),
            description = "Heap sort complete! Array is now sorted."
        ))
    }

    private suspend fun heapify(
        arr: MutableList<Int>,
        n: Int,
        i: Int,
        emitter: MutableSharedFlow<AlgorithmEvent>,
    ) {
        var largest = i
        val left = 2 * i + 1
        val right = 2 * i + 2

        if (left < n) {
            emitter.emit(AlgorithmEvent.Compare(
                indices = left to largest,
                description = DescriptionUtils.compare(left, largest, arr),
                pseudocodeLine = 5
            ))
            if (arr[left] > arr[largest]) largest = left
        }
        if (right < n) {
            emitter.emit(AlgorithmEvent.Compare(
                indices = right to largest,
                description = DescriptionUtils.compare(right, largest, arr),
                pseudocodeLine = 5
            ))
            if (arr[right] > arr[largest]) largest = right
        }

        if (largest != i) {
            emitter.emit(AlgorithmEvent.Swap(
                indices = i to largest,
                description = DescriptionUtils.swap(i, largest, arr),
                pseudocodeLine = 5
            ))
            arr.swapAt(i, largest)
            heapify(arr, n, largest, emitter)
        }
    }
}
