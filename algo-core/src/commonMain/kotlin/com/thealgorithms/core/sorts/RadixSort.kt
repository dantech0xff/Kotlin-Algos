package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.DescriptionUtils
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class RadixSort {
    fun sort(arr: List<Int>): List<Int> {
        if (arr.isEmpty()) return emptyList()

        val min = arr.minOrNull()!!
        val shifted = arr.map { it - min }
        val maxVal = shifted.maxOrNull()!!

        var result = shifted
        var exp = 1

        while (maxVal / exp > 0) {
            result = countingSortByDigit(result, exp)
            exp *= 10
        }

        return result.map { it + min }
    }

    private fun countingSortByDigit(arr: List<Int>, exp: Int): List<Int> {
        val output = MutableList(arr.size) { 0 }
        val count = IntArray(10)

        for (num in arr) {
            count[(num / exp) % 10]++
        }

        for (i in 1 until 10) {
            count[i] += count[i - 1]
        }

        for (i in (arr.size - 1) downTo 0) {
            val digit = (arr[i] / exp) % 10
            count[digit]--
            output[count[digit]] = arr[i]
        }

        return output
    }
}

class RadixSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        if (input.isEmpty()) {
            emitter.emit(AlgorithmEvent.Start(input))
            emitter.emit(AlgorithmEvent.Complete(
                result = input,
                description = "Radix sort complete! Empty array."
            ))
            return
        }

        val arr = input.toMutableList()
        emitter.emit(AlgorithmEvent.Start(input))

        val min = arr.minOrNull()!!
        for (i in arr.indices) {
            arr[i] = arr[i] - min
        }

        val maxVal = arr.maxOrNull()!!
        var exp = 1

        while (maxVal / exp > 0) {
            val output = MutableList(arr.size) { 0 }
            val count = IntArray(10)

            for (num in arr) {
                count[(num / exp) % 10]++
            }

            for (i in 1 until 10) {
                count[i] += count[i - 1]
            }

            for (i in (arr.size - 1) downTo 0) {
                val digit = (arr[i] / exp) % 10
                count[digit]--
                output[count[digit]] = arr[i]
            }

            for (i in arr.indices) {
                arr[i] = output[i]
                emitter.emit(AlgorithmEvent.Select(i))
                emitter.emit(AlgorithmEvent.Overwrite(
                    index = i,
                    newValue = arr[i] + min,
                    description = DescriptionUtils.overwrite(i, arr[i] + min),
                    pseudocodeLine = 5
                ))
            }

            exp *= 10
        }

        for (i in arr.indices) {
            arr[i] = arr[i] + min
        }

        emitter.emit(AlgorithmEvent.Complete(
            result = arr.toList(),
            description = "Radix sort complete! Array is now sorted."
        ))
    }
}
