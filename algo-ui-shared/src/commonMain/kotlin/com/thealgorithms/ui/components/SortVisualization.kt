package com.thealgorithms.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.thealgorithms.shared.HighlightReason
import com.thealgorithms.viz.AlgorithmSnapshot

// ── Colour palette ──────────────────────────────────────────────────

private val DEFAULT_COLOR = Color(0xFF4ECDC4)      // teal
private val COMPARING_COLOR = Color(0xFFFFD93D)     // bright yellow
private val SWAPPING_COLOR = Color(0xFFFF6B6B)      // coral red
private val PIVOTING_COLOR = Color(0xFFA855F7)      // electric purple
private val SELECTING_COLOR = Color(0xFF60A5FA)     // sky blue
private val OVERWRITING_COLOR = Color(0xFFF97316)   // orange
private val SORTED_COLOR = Color(0xFF22C55E)        // confident green
private val FOUND_COLOR = Color(0xFF22C55E)         // green
private val RANGE_COLOR = Color(0xFF818CF8)         // indigo
private val PROBING_COLOR = Color(0xFF60A5FA)       // sky blue (shared)

private val BG_COLOR = Color(0xFF1A1A2E)

private fun Color.darker(factor: Float = 0.6f): Color =
    Color(red = red * factor, green = green * factor, blue = blue * factor)

private fun barColorFor(
    index: Int,
    highlights: Map<Int, HighlightReason>,
    sorted: Set<Int>,
): Color = when (highlights[index]) {
    HighlightReason.COMPARING  -> COMPARING_COLOR
    HighlightReason.SWAPPING   -> SWAPPING_COLOR
    HighlightReason.PIVOTING   -> PIVOTING_COLOR
    HighlightReason.SELECTING  -> SELECTING_COLOR
    HighlightReason.OVERWRITING -> OVERWRITING_COLOR
    HighlightReason.FOUND      -> FOUND_COLOR
    HighlightReason.RANGE      -> RANGE_COLOR
    HighlightReason.SORTED     -> SORTED_COLOR
    HighlightReason.PROBING    -> PROBING_COLOR
    null                       -> if (index in sorted) SORTED_COLOR else DEFAULT_COLOR
}

// ── Main composable ─────────────────────────────────────────────────

@Composable
fun SortVisualization(snapshot: AlgorithmSnapshot, modifier: Modifier = Modifier) {
    val animMap = remember {
        mutableStateMapOf<Int, Animatable<Float, AnimationVector1D>>()
    }
    val textMeasurer = rememberTextMeasurer()

    val maxVal = snapshot.arrayState.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
    val showLabels = snapshot.arrayState.size in 1..25

    // ── Drive animated heights toward current snapshot ──
    snapshot.arrayState.forEachIndexed { index, value ->
        val target = value.toFloat() / maxVal
        val anim = animMap.getOrPut(index) { Animatable(target) }
        LaunchedEffect(index, target) {
            anim.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
    }

    // Prune stale indices when array shrinks or resets
    val liveKeys = snapshot.arrayState.indices.toSet()
    animMap.keys.toList().forEach { if (it !in liveKeys) animMap.remove(it) }

    // ── Canvas drawing ───────────────────────────────────────────────
    Canvas(modifier = modifier) {
        if (snapshot.arrayState.isEmpty()) return@Canvas

        // Dark background
        drawRect(BG_COLOR)

        // Subtle horizontal grid lines
        for (i in 1..4) {
            val y = size.height * i / 5f
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
        }

        val count = snapshot.arrayState.size
        val barWidth = size.width / count
        val gap = when {
            count > 50 -> 1f
            count > 25 -> 2f
            else -> 3f
        }
        val radius = when {
            count > 50 -> 0f
            count > 25 -> 2f
            else -> 6f
        }
        val topPad = if (showLabels) 24f else 0f
        val drawH = size.height - topPad

        val labelStyle = TextStyle(
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
        )

        snapshot.arrayState.forEachIndexed { index, value ->
            val fraction = animMap[index]?.value ?: (value.toFloat() / maxVal)
            val barH = fraction * drawH * 0.92f
            if (barH < 1f) return@forEachIndexed // skip invisible bars

            val w = (barWidth - gap).coerceAtLeast(1f)
            val x = index * barWidth + gap / 2f
            val y = drawH - barH + topPad
            val tl = Offset(x, y)
            val sz = Size(w, barH)

            val color = barColorFor(index, snapshot.highlights, snapshot.sortedIndices)
            val reason = snapshot.highlights[index]

            // ── Glow halo for active comparisons / swaps ──
            if (reason == HighlightReason.COMPARING || reason == HighlightReason.SWAPPING) {
                drawRoundRect(
                    color = color.copy(alpha = 0.3f),
                    topLeft = Offset(tl.x - 2f, tl.y - 2f),
                    size = Size(sz.width + 4f, sz.height + 4f),
                    cornerRadius = CornerRadius(radius + 2f),
                )
            }

            // ── Drop shadow for depth ──
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.2f),
                topLeft = tl + Offset(2f, 2f),
                size = sz,
                cornerRadius = CornerRadius(radius),
            )

            // ── Gradient-filled bar ──
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color, color.darker()),
                    startY = tl.y,
                    endY = tl.y + sz.height,
                ),
                topLeft = tl,
                size = sz,
                cornerRadius = CornerRadius(radius),
            )

            // ── Value label above bar (≤25 bars) ──
            if (showLabels) {
                val measured = textMeasurer.measure(value.toString(), labelStyle)
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(
                        x = x + w / 2f - measured.size.width / 2f,
                        y = y - 18f,
                    ),
                )
            }
        }
    }
}
