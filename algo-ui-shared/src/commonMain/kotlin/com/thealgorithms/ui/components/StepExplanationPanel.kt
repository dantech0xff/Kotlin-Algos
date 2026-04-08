package com.thealgorithms.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thealgorithms.ui.theme.VizColors

@Composable
fun StepExplanationPanel(
    description: String,
    progress: Pair<Int, Int>,
    modifier: Modifier = Modifier
) {
    val (current, total) = progress

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Text(
            text = "Step $current of $total",
            style = MaterialTheme.typography.labelSmall,
            color = VizColors.textMuted
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = description.ifEmpty { "Waiting for next step..." },
            style = MaterialTheme.typography.bodySmall,
            color = if (description.isNotEmpty()) VizColors.textPrimary else VizColors.textMuted
        )
    }
}
