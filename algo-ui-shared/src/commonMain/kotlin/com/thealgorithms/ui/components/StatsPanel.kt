package com.thealgorithms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thealgorithms.shared.PlaybackState
import com.thealgorithms.ui.theme.VizColors
import com.thealgorithms.viz.AlgorithmSnapshot

@Composable
fun StatsPanel(
    algorithmName: String,
    algorithmDescription: String?,
    snapshot: AlgorithmSnapshot,
    playbackState: PlaybackState,
    progress: Pair<Int, Int>,
    modifier: Modifier = Modifier
) {
    val (currentEvent, totalEvents) = progress

    Surface(
        modifier = modifier,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        color = VizColors.surfaceDark
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Row 1: Algorithm name + state badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = algorithmName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VizColors.textPrimary
                )
                StateBadge(playbackState)
            }

            // Row 2: Complexity badge
            if (algorithmDescription != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = algorithmDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = VizColors.textMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(Modifier.height(12.dp))

            // Row 3: Live stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatChip("Comparisons", snapshot.comparisons.toString(), VizColors.chipComparisons)
                StatChip("Swaps", snapshot.swaps.toString(), VizColors.chipSwaps)
                StatChip("Elements", snapshot.arrayState.size.toString(), VizColors.chipElements)
            }

            Spacer(Modifier.height(8.dp))

            // Row 4: Progress bar + event counter
            val progressFraction = if (totalEvents > 0) currentEvent.toFloat() / totalEvents else 0f
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = VizColors.progressTrack,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Event $currentEvent / $totalEvents",
                style = MaterialTheme.typography.labelSmall,
                color = VizColors.textMuted
            )
        }
    }
}

@Composable
private fun StateBadge(state: PlaybackState) {
    val (label, bgColor, textColor) = when (state) {
        is PlaybackState.Playing -> Triple("Playing", VizColors.sorted.copy(alpha = 0.2f), VizColors.sorted)
        is PlaybackState.Paused -> Triple("Paused", VizColors.comparing.copy(alpha = 0.2f), VizColors.comparing)
        is PlaybackState.Complete -> Triple("Complete", VizColors.selecting.copy(alpha = 0.2f), VizColors.selecting)
        else -> Triple("Stopped", VizColors.textMuted.copy(alpha = 0.2f), VizColors.textMuted)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = "● $label",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun StatChip(label: String, value: String, accentColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = accentColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VizColors.textMuted
        )
    }
}
