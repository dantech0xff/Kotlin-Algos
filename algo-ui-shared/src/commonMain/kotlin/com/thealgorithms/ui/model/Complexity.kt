package com.thealgorithms.ui.model

data class Complexity(
    val best: String,
    val average: String,
    val worst: String
)

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }
