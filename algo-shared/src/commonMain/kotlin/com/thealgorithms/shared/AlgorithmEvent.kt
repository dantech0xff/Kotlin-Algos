package com.thealgorithms.shared

enum class HighlightReason {
    COMPARING, SWAPPING, PIVOTING, SELECTING, PROBING,
    FOUND, RANGE, OVERWRITING, SORTED
}

sealed interface AlgorithmEvent {
    data class Start(val data: List<Int>) : AlgorithmEvent
    data class Compare(val indices: Pair<Int, Int>) : AlgorithmEvent
    data class Swap(val indices: Pair<Int, Int>) : AlgorithmEvent
    data class Select(val index: Int) : AlgorithmEvent
    data class Deselect(val index: Int) : AlgorithmEvent
    data class Pivot(val index: Int) : AlgorithmEvent
    data class Overwrite(val index: Int, val newValue: Int) : AlgorithmEvent
    data class Complete(val result: List<Int>) : AlgorithmEvent
    data class Probe(val index: Int) : AlgorithmEvent
    data class Found(val index: Int) : AlgorithmEvent
    data object NotFound : AlgorithmEvent
    data class RangeCheck(val low: Int, val high: Int) : AlgorithmEvent
}
