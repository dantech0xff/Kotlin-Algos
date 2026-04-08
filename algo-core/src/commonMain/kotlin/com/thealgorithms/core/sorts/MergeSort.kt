package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.DescriptionUtils
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SortAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class MergeSort : SortAlgorithm {
    override fun <T : Comparable<T>> sort(list: List<T>): List<T> {
        val arr = list.toMutableList()
        doSort(arr, 0, arr.size - 1)
        return arr.toList()
    }

    private fun <T : Comparable<T>> doSort(arr: MutableList<T>, left: Int, right: Int) {
        if (left < right) {
            val mid = (left + right) ushr 1
            doSort(arr, left, mid)
            doSort(arr, mid + 1, right)
            merge(arr, left, mid, right)
        }
    }

    private fun <T : Comparable<T>> merge(arr: MutableList<T>, left: Int, mid: Int, right: Int) {
        val temp = arr.slice(left..right).toMutableList()
        var i = 0
        var j = mid - left + 1
        var k = left

        while (i <= mid - left && j <= right - left) {
            if (temp[j] < temp[i]) {
                arr[k] = temp[j]
                j++
            } else {
                arr[k] = temp[i]
                i++
            }
            k++
        }
        while (i <= mid - left) { arr[k] = temp[i]; i++; k++ }
        while (j <= right - left) { arr[k] = temp[j]; j++; k++ }
    }
}

class MergeSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        val arr = input.toMutableList()
        emitter.emit(AlgorithmEvent.Start(input))
        mergeSort(arr, 0, arr.size - 1, emitter)
        emitter.emit(AlgorithmEvent.Complete(
            result = arr.toList(),
            description = "Merge sort complete! Array is now sorted."
        ))
    }

    private suspend fun mergeSort(
        arr: MutableList<Int>,
        left: Int,
        right: Int,
        emitter: MutableSharedFlow<AlgorithmEvent>,
    ) {
        if (left < right) {
            val mid = (left + right) ushr 1
            mergeSort(arr, left, mid, emitter)
            mergeSort(arr, mid + 1, right, emitter)
            merge(arr, left, mid, right, emitter)
        }
    }

    private suspend fun merge(
        arr: MutableList<Int>,
        left: Int,
        mid: Int,
        right: Int,
        emitter: MutableSharedFlow<AlgorithmEvent>,
    ) {
        val temp = arr.slice(left..right).toMutableList()
        var i = 0
        var j = mid - left + 1
        var k = left

        while (i <= mid - left && j <= right - left) {
            emitter.emit(AlgorithmEvent.Compare(
                indices = left + i to left + j,
                description = "Comparing left[${left + i}]=${temp[i]} with right[${left + j}]=${temp[j]}",
                pseudocodeLine = 6
            ))
            if (temp[i] <= temp[j]) {
                arr[k] = temp[i]
                emitter.emit(AlgorithmEvent.Overwrite(
                    index = k,
                    newValue = temp[i],
                    description = DescriptionUtils.overwrite(k, temp[i]),
                    pseudocodeLine = 6
                ))
                i++
            } else {
                arr[k] = temp[j]
                emitter.emit(AlgorithmEvent.Overwrite(
                    index = k,
                    newValue = temp[j],
                    description = DescriptionUtils.overwrite(k, temp[j]),
                    pseudocodeLine = 6
                ))
                j++
            }
            k++
        }
        while (i <= mid - left) {
            arr[k] = temp[i]
            emitter.emit(AlgorithmEvent.Overwrite(
                index = k,
                newValue = temp[i],
                description = DescriptionUtils.overwrite(k, temp[i]),
                pseudocodeLine = 6
            ))
            i++; k++
        }
        while (j <= right - left) {
            arr[k] = temp[j]
            emitter.emit(AlgorithmEvent.Overwrite(
                index = k,
                newValue = temp[j],
                description = DescriptionUtils.overwrite(k, temp[j]),
                pseudocodeLine = 6
            ))
            j++; k++
        }
    }
}
