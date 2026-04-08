package com.thealgorithms.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thealgorithms.ui.model.AlgorithmInfo
import com.thealgorithms.ui.model.Difficulty
import com.thealgorithms.ui.theme.VizColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComplexityPanel(
    info: AlgorithmInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)
    ) {
        // Time Complexity
        Text(
            text = "⏱ Time Complexity",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = VizColors.textAccent
        )
        Spacer(Modifier.height(6.dp))
        ComplexityRow("Best", info.timeComplexity.best)
        ComplexityRow("Average", info.timeComplexity.average)
        ComplexityRow("Worst", info.timeComplexity.worst)

        Spacer(Modifier.height(10.dp))

        // Space Complexity
        Text(
            text = "💾 Space Complexity",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = VizColors.textAccent
        )
        Spacer(Modifier.height(4.dp))
        ComplexityRow("Space", info.spaceComplexity)

        Spacer(Modifier.height(10.dp))

        // Stability
        Row(verticalAlignment = Alignment.CenterVertically) {
            val stableColor = if (info.isStable) VizColors.sorted else VizColors.swapping
            Text(
                text = if (info.isStable) "✓ Stable" else "✗ Unstable",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = stableColor
            )
        }

        Spacer(Modifier.height(10.dp))

        // Difficulty
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Difficulty: ",
                style = MaterialTheme.typography.labelMedium,
                color = VizColors.textMuted
            )
            val filled = when (info.difficulty) {
                Difficulty.BEGINNER -> 1
                Difficulty.INTERMEDIATE -> 2
                Difficulty.ADVANCED -> 3
            }
            repeat(3) { i ->
                Text(
                    text = if (i < filled) "★" else "☆",
                    color = if (i < filled) VizColors.comparing else VizColors.textMuted,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = info.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = VizColors.textMuted
            )
        }

        // Tags
        if (info.tags.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                info.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VizColors.surfaceDark,
                        modifier = Modifier.border(
                            1.dp,
                            VizColors.divider,
                            RoundedCornerShape(12.dp)
                        )
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = VizColors.textMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplexityRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VizColors.textMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = VizColors.textPrimary
        )
    }
}
