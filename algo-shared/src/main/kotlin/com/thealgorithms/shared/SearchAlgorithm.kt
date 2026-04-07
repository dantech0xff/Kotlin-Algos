package com.thealgorithms.shared

interface SearchAlgorithm {
    fun <T : Comparable<T>> find(array: List<T>, key: T): Int
}
