package com.thealgorithms.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.thealgorithms.viz.AlgorithmSnapshot

private val DEFAULT_BAR_COLOR = Color(0xFF4ECDC4)
private val HIGHLIGHTED_BAR_COLOR = Color(0xFFFF6B35)

@Composable
fun SortVisualization(snapshot: AlgorithmSnapshot, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (snapshot.arrayState.isEmpty()) return@Canvas

        val barWidth = size.width / snapshot.arrayState.size
        val maxVal = snapshot.arrayState.maxOrNull()?.toFloat() ?: 1f
        val cornerRadius = if (snapshot.arrayState.size > 25) 0f else 4f

        snapshot.arrayState.forEachIndexed { index, value ->
            val barHeight = (value.toFloat() / maxVal) * size.height * 0.9f
            val topLeft = Offset(index * barWidth, size.height - barHeight)
            val barSize = Size((barWidth - 2f).coerceAtLeast(1f), barHeight)

            val color = if (index in snapshot.highlightedIndices) {
                HIGHLIGHTED_BAR_COLOR
            } else {
                DEFAULT_BAR_COLOR
            }

            drawRoundRect(
                color = color,
                topLeft = topLeft,
                size = barSize,
                cornerRadius = CornerRadius(cornerRadius)
            )
        }
    }
}
