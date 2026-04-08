package com.thealgorithms.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.thealgorithms.ui.theme.VizColors
import com.thealgorithms.viz.AlgorithmSnapshot

private fun Color.darker(factor: Float = 0.6f): Color =
    Color(red = red * factor, green = green * factor, blue = blue * factor)

private fun hslToColor(h: Float, s: Float, l: Float): Color {
    val hue = ((h % 360f) + 360f) % 360f / 360f
    val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
    val p = 2 * l - q
    fun channel(t: Float): Float {
        val tt = t.coerceIn(0f, 1f)
        return when {
            tt < 1f / 6f -> p + (q - p) * 6f * tt
            tt < 0.5f -> q
            tt < 2f / 3f -> p + (q - p) * (2f / 3f - tt) * 6f
            else -> p
        }
    }
    return Color(
        red = channel(hue + 1f / 3f),
        green = channel(hue),
        blue = channel(hue - 1f / 3f)
    )
}

private fun barColorFor(
    index: Int,
    highlights: Map<Int, HighlightReason>,
    sorted: Set<Int>,
): Color = when (highlights[index]) {
    HighlightReason.COMPARING  -> VizColors.comparing
    HighlightReason.SWAPPING   -> VizColors.swapping
    HighlightReason.PIVOTING   -> VizColors.pivoting
    HighlightReason.SELECTING  -> VizColors.selecting
    HighlightReason.OVERWRITING -> VizColors.overwriting
    HighlightReason.FOUND      -> VizColors.found
    HighlightReason.RANGE      -> VizColors.rangeHighlight
    HighlightReason.SORTED     -> VizColors.sorted
    HighlightReason.PROBING    -> VizColors.probing
    null                       -> if (index in sorted) VizColors.sorted else VizColors.defaultElement
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

    // Detect completion: all elements sorted
    val isComplete = snapshot.arrayState.isNotEmpty() &&
        snapshot.sortedIndices.size == snapshot.arrayState.size

    // Animated sweep for rainbow celebration
    val sweepProgress by animateFloatAsState(
        targetValue = if (isComplete) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1500)
    )

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
        drawRect(VizColors.canvasDark)

        // Subtle horizontal grid lines
        for (i in 1..4) {
            val y = size.height * i / 5f
            drawLine(
                color = VizColors.textPrimary.copy(alpha = 0.08f),
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
        val bottomPad = if (showLabels) 16f else 0f
        val drawH = size.height - topPad - bottomPad

        val labelStyle = TextStyle(
            color = VizColors.textPrimary.copy(alpha = 0.85f),
            fontSize = 11.sp,
        )
        val indexStyle = TextStyle(
            color = VizColors.textMuted,
            fontSize = 9.sp,
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

            val color = if (isComplete && sweepProgress > 0f) {
                // Rainbow hue: sweep from left to right
                val normalizedPos = index.toFloat() / count.toFloat()
                val sweepOffset = sweepProgress * 1.5f
                hslToColor(((normalizedPos + sweepOffset) % 1f) * 360f, 0.75f, 0.55f)
            } else {
                barColorFor(index, snapshot.highlights, snapshot.sortedIndices)
            }
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
                color = VizColors.canvasDark.copy(alpha = 0.5f),
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

                // Index label below bar
                val idxMeasured = textMeasurer.measure(index.toString(), indexStyle)
                drawText(
                    textLayoutResult = idxMeasured,
                    topLeft = Offset(
                        x = x + w / 2f - idxMeasured.size.width / 2f,
                        y = drawH + topPad + 2f,
                    ),
                )
            }
        }
    }
}
