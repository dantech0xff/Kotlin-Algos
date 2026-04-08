package com.thealgorithms.ui.model

data class PseudocodeLine(
    val text: String,
    val indentLevel: Int = 0,
    val lineNumber: Int
)
