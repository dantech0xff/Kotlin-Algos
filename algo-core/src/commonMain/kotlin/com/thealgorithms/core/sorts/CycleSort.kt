package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.isLessThan
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SortAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class CycleSort : SortAlgorithm {
    override fun <T : Comparable<T>> sort(list: List<T>): List<T> {
        val arr = list.toMutableList()
        val n = arr.size

        for (cycleStart in 0 until n - 1) {
            var item = arr[cycleStart]
            var pos = cycleStart

            // Find position where we put the item
            for (i in (cycleStart + 1) until n) {
                if (arr[i].isLessThan(item)) pos++
            }

            // If item is already in correct position
            if (pos == cycleStart) continue

            // Skip duplicates
            while (item == arr[pos]) pos++

            // Put item to its right position
            if (pos != cycleStart) {
                val temp = arr[pos]
                arr[pos] = item
                item = temp
            }

            // Rotate rest of the cycle
            while (pos != cycleStart) {
                pos = cycleStart

                for (i in (cycleStart + 1) until n) {
                    if (arr[i].isLessThan(item)) pos++
                }

                while (item == arr[pos]) pos++

                val temp = arr[pos]
                arr[pos] = item
                item = temp
            }
        }

        return arr.toList()
    }
}

class CycleSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        val arr = input.toMutableList()
        val n = arr.size
        emitter.emit(AlgorithmEvent.Start(input))

        for (cycleStart in 0 until n - 1) {
            var item = arr[cycleStart]
            emitter.emit(AlgorithmEvent.Select(cycleStart))
            var pos = cycleStart

            // Find position where we put the item
            for (i in (cycleStart + 1) until n) {
                emitter.emit(AlgorithmEvent.Compare(cycleStart to i))
                if (arr[i] < item) pos++
            }

            // If item is already in correct position
            if (pos == cycleStart) {
                emitter.emit(AlgorithmEvent.Deselect(cycleStart))
                continue
            }

            // Skip duplicates
            while (item == arr[pos]) pos++

            // Put item to its right position
            if (pos != cycleStart) {
                emitter.emit(AlgorithmEvent.Overwrite(pos, item))
                val temp = arr[pos]
                arr[pos] = item
                item = temp
            }

            // Rotate rest of the cycle
            while (pos != cycleStart) {
                pos = cycleStart

                for (i in (cycleStart + 1) until n) {
                    if (arr[i] < item) pos++
                }

                while (item == arr[pos]) pos++

                emitter.emit(AlgorithmEvent.Overwrite(pos, item))
                val temp = arr[pos]
                arr[pos] = item
                item = temp
            }

            emitter.emit(AlgorithmEvent.Deselect(cycleStart))
        }

        emitter.emit(AlgorithmEvent.Complete(arr.toList()))
    }
}
