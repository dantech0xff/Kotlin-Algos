package com.thealgorithms.viz

data class AlgorithmSnapshot(
    val arrayState: List<Int>,
    val highlightedIndices: Set<Int> = emptySet(),
    val comparisons: Int = 0,
    val swaps: Int = 0
)
