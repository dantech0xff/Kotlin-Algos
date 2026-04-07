package com.thealgorithms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thealgorithms.viz.AlgorithmSnapshot

@Composable
fun SearchVisualization(snapshot: AlgorithmSnapshot, modifier: Modifier = Modifier) {
    if (snapshot.arrayState.isEmpty()) return

    val cellCount = (snapshot.arrayState.size / 8).coerceIn(1, 6)
    val highlighted = snapshot.highlightedIndices

    LazyVerticalGrid(
        columns = GridCells.Fixed(cellCount),
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(snapshot.arrayState) { index, value ->
            val (bgColor, borderColor) = resolveCellColors(index, highlighted)
            CellBox(value, bgColor, borderColor)
        }
    }
}

@Composable
private fun CellBox(
    value: Int,
    bgColor: Color,
    borderColor: Color
) {
    Box(
        modifier = Modifier
            .aspectRatio(1.5f)
            .background(bgColor, shape = MaterialTheme.shapes.medium)
            .border(1.dp, borderColor, MaterialTheme.shapes.medium)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun resolveCellColors(index: Int, highlighted: Set<Int>): Pair<Color, Color> {
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline
    if (highlighted.isEmpty()) return surface to outline

    // When highlighted set is large (RangeCheck), treat as range highlight
    val bgColor = when {
        highlighted.size > 3 -> Color(0xFFBBDEFB) // blue range
        else -> when {
            highlighted.contains(index) && highlighted.size == 1 -> Color(0xFFA5D6A7) // found / single
            highlighted.contains(index) -> Color(0xFFFFE082) // probed / compared
            else -> surface
        }
    }
    val borderCol = if (highlighted.contains(index)) {
        Color(0xFF1565C0)
    } else {
        outline
    }
    return bgColor to borderCol
}
