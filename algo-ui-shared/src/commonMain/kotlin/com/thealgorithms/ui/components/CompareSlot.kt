package com.thealgorithms.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thealgorithms.ui.model.CompareSlotState
import com.thealgorithms.ui.theme.VizColors

@Composable
fun CompareSlot(
    slotState: CompareSlotState,
    slotIndex: Int,
    modifier: Modifier = Modifier
) {
    val slotColor = VizColors.compareSlotColors.getOrElse(slotIndex) { VizColors.compareSlotColors[0] }

    Surface(
        modifier = modifier.padding(2.dp),
        shape = RoundedCornerShape(6.dp),
        color = VizColors.surfaceDark,
        border = BorderStroke(1.dp, VizColors.divider)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(slotColor, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = slotState.algorithm.name,
                    color = VizColors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (slotState.isComplete) {
                    Text(
                        text = "✓",
                        color = VizColors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp)
                    )
                }
            }

            // Mini visualization
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                SortVisualization(
                    snapshot = slotState.snapshot,
                    compact = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${slotState.comparisons} cmp · ${slotState.swaps} swp · Step ${slotState.eventIndex}/${slotState.totalEvents}",
                    color = VizColors.textMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
