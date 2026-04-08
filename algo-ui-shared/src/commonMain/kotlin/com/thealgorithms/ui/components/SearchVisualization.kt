package com.thealgorithms.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thealgorithms.shared.HighlightReason
import com.thealgorithms.ui.theme.VizColors
import com.thealgorithms.viz.AlgorithmSnapshot

// ── Public composable ──────────────────────────────────────────────────────

@Composable
fun SearchVisualization(
    snapshot: AlgorithmSnapshot,
    modifier: Modifier = Modifier,
    searchKey: Int? = null
) {
    if (snapshot.arrayState.isEmpty()) return

    val columns = (snapshot.arrayState.size / 8).coerceIn(1, 6)

    Box(
        modifier = modifier
            .background(VizColors.gridDark, MaterialTheme.shapes.large)
            .padding(12.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            // LazyVerticalGrid is nested in a scrollable parent in some screens;
            // disabling its own scrolling avoids nested-scroll conflicts.
            userScrollEnabled = false
        ) {
            itemsIndexed(snapshot.arrayState) { index, value ->
                val reason = snapshot.highlights[index]
                val isProbed = reason == HighlightReason.PROBING || reason == HighlightReason.COMPARING
                val isFound = reason == HighlightReason.FOUND
                val isSearchKeyMatch = searchKey != null && value == searchKey

                SearchCell(
                    value = value,
                    index = index,
                    reason = reason,
                    isProbed = isProbed,
                    isFound = isFound,
                    showKeyDot = isSearchKeyMatch && !isFound
                )
            }
        }
    }
}

// ── Single cell ────────────────────────────────────────────────────────────

@Composable
private fun SearchCell(
    value: Int,
    index: Int,
    reason: HighlightReason?,
    isProbed: Boolean,
    isFound: Boolean,
    showKeyDot: Boolean
) {
    // Pulse animation for probed / comparing cells
    val scale by animateFloatAsState(
        targetValue = if (isProbed) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = Spring.StiffnessMedium
        )
    )

    val bgColor = cellBackground(reason)
    val borderColor = cellBorder(reason)
    val borderWid = if (isFound) 2.5.dp else 1.dp
    val valueWeight = if (reason != null) FontWeight.Bold else FontWeight.Medium
    val valueColor = if (reason != null) Color.White else VizColors.textMuted

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        contentAlignment = Alignment.Center
    ) {
        // Colored glow behind the cell for FOUND (KMP-safe alternative to shadow)
        if (isFound) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        drawCircle(
                            color = VizColors.found.copy(alpha = 0.25f),
                            radius = size.minDimension * 0.7f
                        )
                    }
            )
        }

        Box(
            modifier = Modifier
                .aspectRatio(1.5f)
                .background(bgColor, MaterialTheme.shapes.medium)
                .border(borderWid, borderColor, MaterialTheme.shapes.medium)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = valueWeight,
                        color = valueColor,
                        textAlign = TextAlign.Center
                    )

                    // Small dot indicating this cell's value matches the search key
                    if (showKeyDot) {
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(6.dp)
                                .background(VizColors.keyDot, CircleShape)
                        )
                    }
                }

                // Index label
                Text(
                    text = "#$index",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = VizColors.textMuted.copy(alpha = 0.6f),
                )
            }
        }
    }
}

// ── Color resolution helpers ───────────────────────────────────────────────

private fun cellBackground(reason: HighlightReason?): Color = when (reason) {
    HighlightReason.PROBING,
    HighlightReason.COMPARING -> VizColors.probing
    HighlightReason.FOUND     -> VizColors.found
    HighlightReason.RANGE     -> VizColors.rangeHighlight
    HighlightReason.SORTED    -> VizColors.sorted
    HighlightReason.PIVOTING  -> VizColors.pivoting
    HighlightReason.SWAPPING  -> VizColors.swapping
    HighlightReason.SELECTING -> VizColors.selecting
    HighlightReason.OVERWRITING -> VizColors.overwriting
    null                      -> VizColors.cellDefault
}

private fun cellBorder(reason: HighlightReason?): Color = when (reason) {
    HighlightReason.PROBING,
    HighlightReason.COMPARING -> VizColors.probing.copy(alpha = 0.7f)
    HighlightReason.FOUND     -> VizColors.found
    HighlightReason.RANGE     -> VizColors.rangeHighlight.copy(alpha = 0.7f)
    HighlightReason.SORTED    -> VizColors.sorted.copy(alpha = 0.5f)
    HighlightReason.PIVOTING  -> VizColors.pivoting.copy(alpha = 0.6f)
    HighlightReason.SWAPPING  -> VizColors.swapping.copy(alpha = 0.6f)
    HighlightReason.SELECTING -> VizColors.selecting.copy(alpha = 0.5f)
    HighlightReason.OVERWRITING -> VizColors.overwriting.copy(alpha = 0.5f)
    null                      -> VizColors.cellBorder
}
