package com.thealgorithms.core.utils

fun <T> MutableList<T>.swapAt(i: Int, j: Int) {
    if (i != j) {
        val temp = this[i]
        this[i] = this[j]
        this[j] = temp
    }
}

fun <T : Comparable<T>> T.isLessThan(other: T): Boolean = this.compareTo(other) < 0

fun <T : Comparable<T>> T.isGreaterThan(other: T): Boolean = this.compareTo(other) > 0

fun <T : Comparable<T>> List<T>.isSorted(): Boolean {
    for (i in 1 until size) {
        if (this[i] < this[i - 1]) return false
    }
    return true
}
