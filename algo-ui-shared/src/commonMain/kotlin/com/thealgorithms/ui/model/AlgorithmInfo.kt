package com.thealgorithms.ui.model

data class AlgorithmInfo(
    val name: String,
    val category: AlgorithmCategory,
    val description: String,
    val visualizer: Any,
    val difficulty: Difficulty = Difficulty.BEGINNER,
    val timeComplexity: Complexity = Complexity("—", "—", "—"),
    val spaceComplexity: String = "—",
    val isStable: Boolean = false,
    val tags: List<String> = emptyList(),
    val pseudocode: List<PseudocodeLine> = emptyList()
)
