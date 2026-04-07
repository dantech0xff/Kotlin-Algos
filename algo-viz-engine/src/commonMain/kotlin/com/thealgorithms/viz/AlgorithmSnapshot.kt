package com.thealgorithms.viz

import com.thealgorithms.shared.HighlightReason

data class AlgorithmSnapshot(
    val arrayState: List<Int>,
    val highlights: Map<Int, HighlightReason> = emptyMap(),
    val comparisons: Int = 0,
    val swaps: Int = 0,
    val sortedIndices: Set<Int> = emptySet()
) {
    /** Legacy accessor for backward compatibility during migration */
    val highlightedIndices: Set<Int> get() = highlights.keys
}
