package com.thealgorithms.core.sorts

import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Counting Sort — a non-comparison, integer sorting algorithm.
 *
 * Time:  O(n + k) where k = max − min + 1
 * Space: O(k)
 *
 * Works on any integer range (including negatives) by offsetting with min.
 * Does NOT implement [com.thealgorithms.shared.SortAlgorithm] because that
 * interface requires a generic [Comparable] signature and counting sort is
 * inherently integer-only.
 */
class CountingSort {
    fun sort(arr: List<Int>): List<Int> {
        if (arr.isEmpty()) return emptyList()
        val min = arr.minOrNull()!!
        val max = arr.maxOrNull()!!
        val count = IntArray(max - min + 1)

        for (num in arr) {
            count[num - min]++
        }

        val result = MutableList(arr.size) { 0 }
        var idx = 0
        for (i in count.indices) {
            repeat(count[i]) {
                result[idx++] = i + min
            }
        }
        return result
    }
}

class CountingSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        if (input.isEmpty()) {
            emitter.emit(AlgorithmEvent.Start(input))
            emitter.emit(AlgorithmEvent.Complete(input))
            return
        }

        val arr = input.toMutableList()
        emitter.emit(AlgorithmEvent.Start(input))

        val min = arr.minOrNull()!!
        val max = arr.maxOrNull()!!
        val count = IntArray(max - min + 1)

        // Count occurrences
        for (num in arr) {
            count[num - min]++
        }

        // Reconstruct sorted array with visualisation
        var index = 0
        for (i in count.indices) {
            repeat(count[i]) {
                val value = i + min
                emitter.emit(AlgorithmEvent.Select(index))
                emitter.emit(AlgorithmEvent.Overwrite(index, value))
                arr[index] = value
                index++
            }
        }

        emitter.emit(AlgorithmEvent.Complete(arr.toList()))
    }
}
