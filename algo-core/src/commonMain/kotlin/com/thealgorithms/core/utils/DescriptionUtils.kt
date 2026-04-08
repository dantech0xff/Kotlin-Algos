package com.thealgorithms.core.utils

object DescriptionUtils {
    fun compare(i: Int, j: Int, arr: List<Int>): String =
        "Comparing arr[$i]=${arr[i]} with arr[$j]=${arr[j]}"

    fun swap(i: Int, j: Int, arr: List<Int>): String =
        "Swapping arr[$i]=${arr[i]} and arr[$j]=${arr[j]}"

    fun pivot(index: Int, arr: List<Int>): String =
        "Selected pivot: arr[$index]=${arr[index]}"

    fun overwrite(index: Int, newValue: Int): String =
        "Writing $newValue to position $index"
}
