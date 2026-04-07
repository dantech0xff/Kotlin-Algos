package com.thealgorithms.shared

interface SortAlgorithm {
    fun <T : Comparable<T>> sort(list: List<T>): List<T>
}
