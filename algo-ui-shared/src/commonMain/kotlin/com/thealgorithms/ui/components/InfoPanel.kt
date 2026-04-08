package com.thealgorithms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thealgorithms.shared.PlaybackState
import com.thealgorithms.ui.model.AlgorithmInfo
import com.thealgorithms.ui.theme.VizColors
import com.thealgorithms.viz.AlgorithmSnapshot

@Composable
fun InfoPanel(
    algorithmInfo: AlgorithmInfo?,
    snapshot: AlgorithmSnapshot,
    playbackState: PlaybackState,
    progress: Pair<Int, Int>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(VizColors.surfaceDark)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        if (algorithmInfo == null) {
            // Placeholder
            Text(
                text = "Select an algorithm",
                style = MaterialTheme.typography.bodyMedium,
                color = VizColors.textMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            // Pseudocode section
            if (algorithmInfo.pseudocode.isNotEmpty()) {
                PseudocodePanel(
                    pseudocode = algorithmInfo.pseudocode,
                    activeLine = snapshot.activePseudocodeLine,
                    modifier = Modifier.fillMaxWidth()
                )
                SectionDivider()
            }

            // Step explanation
            StepExplanationPanel(
                description = snapshot.currentDescription,
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
            SectionDivider()

            // Complexity details
            ComplexityPanel(
                info = algorithmInfo,
                modifier = Modifier.fillMaxWidth()
            )
            SectionDivider()

            // Color legend
            LegendPanel(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(10.dp))
    HorizontalDivider(color = VizColors.divider, thickness = 1.dp)
    Spacer(Modifier.height(10.dp))
}
