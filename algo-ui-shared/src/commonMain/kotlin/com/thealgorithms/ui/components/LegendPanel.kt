package com.thealgorithms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thealgorithms.shared.HighlightReason
import com.thealgorithms.ui.theme.VizColors

private data class LegendEntry(
    val reason: HighlightReason,
    val label: String,
    val color: androidx.compose.ui.graphics.Color
)

private val legendEntries = listOf(
    LegendEntry(HighlightReason.COMPARING, "Compare", VizColors.comparing),
    LegendEntry(HighlightReason.SWAPPING, "Swap", VizColors.swapping),
    LegendEntry(HighlightReason.PIVOTING, "Pivot", VizColors.pivoting),
    LegendEntry(HighlightReason.SELECTING, "Select", VizColors.selecting),
    LegendEntry(HighlightReason.OVERWRITING, "Overwrite", VizColors.overwriting),
    LegendEntry(HighlightReason.SORTED, "Sorted", VizColors.sorted),
    LegendEntry(HighlightReason.PROBING, "Probe", VizColors.probing),
    LegendEntry(HighlightReason.FOUND, "Found", VizColors.found),
    LegendEntry(HighlightReason.RANGE, "Range", VizColors.rangeHighlight),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LegendPanel(
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
    ) {
        legendEntries.forEach { entry ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(
                    Modifier
                        .size(8.dp)
                        .background(entry.color, CircleShape)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = VizColors.textMuted
                )
            }
        }
    }
}
