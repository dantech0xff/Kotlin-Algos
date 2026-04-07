package com.thealgorithms.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.thealgorithms.shared.HighlightReason
import com.thealgorithms.viz.AlgorithmSnapshot

// ── Color palette ──────────────────────────────────────────────────────────
private val PROBE_COLOR = Color(0xFFFFD93D)       // yellow – PROBING / COMPARING
private val FOUND_COLOR = Color(0xFF22C55E)        // green – FOUND
private val RANGE_COLOR = Color(0xFF818CF8)        // indigo – RANGE
private val SORTED_COLOR = Color(0xFF34D399)       // emerald – SORTED
private val PIVOT_COLOR = Color(0xFFF472B6)        // pink – PIVOTING
private val SWAP_COLOR = Color(0xFFFB923C)         // orange – SWAPPING
private val DEFAULT_BG = Color(0xFF2D2D44)         // dark surface
private val DEFAULT_BORDER = Color(0xFF4A4A6A)     // muted border
private val GRID_BG = Color(0xFF1E1E32)            // container background
private val SEARCH_KEY_DOT = Color(0xFF38BDF8)     // sky-blue dot

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
            .background(GRID_BG, MaterialTheme.shapes.large)
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
    val valueColor = if (reason != null) Color.White else Color(0xFFB0B0CC)

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
                            color = FOUND_COLOR.copy(alpha = 0.25f),
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
                            .background(SEARCH_KEY_DOT, CircleShape)
                    )
                }
            }
        }
    }
}

// ── Color resolution helpers ───────────────────────────────────────────────

private fun cellBackground(reason: HighlightReason?): Color = when (reason) {
    HighlightReason.PROBING,
    HighlightReason.COMPARING -> PROBE_COLOR
    HighlightReason.FOUND     -> FOUND_COLOR
    HighlightReason.RANGE     -> RANGE_COLOR
    HighlightReason.SORTED    -> SORTED_COLOR
    HighlightReason.PIVOTING  -> PIVOT_COLOR
    HighlightReason.SWAPPING  -> SWAP_COLOR
    HighlightReason.SELECTING -> PROBE_COLOR
    HighlightReason.OVERWRITING -> SWAP_COLOR
    null                      -> DEFAULT_BG
}

private fun cellBorder(reason: HighlightReason?): Color = when (reason) {
    HighlightReason.PROBING,
    HighlightReason.COMPARING -> PROBE_COLOR.copy(alpha = 0.7f)
    HighlightReason.FOUND     -> FOUND_COLOR
    HighlightReason.RANGE     -> RANGE_COLOR.copy(alpha = 0.7f)
    HighlightReason.SORTED    -> SORTED_COLOR.copy(alpha = 0.5f)
    HighlightReason.PIVOTING  -> PIVOT_COLOR.copy(alpha = 0.6f)
    HighlightReason.SWAPPING  -> SWAP_COLOR.copy(alpha = 0.6f)
    HighlightReason.SELECTING -> PROBE_COLOR.copy(alpha = 0.5f)
    HighlightReason.OVERWRITING -> SWAP_COLOR.copy(alpha = 0.5f)
    null                      -> DEFAULT_BORDER
}
