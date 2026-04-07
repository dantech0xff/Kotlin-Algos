package com.thealgorithms.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thealgorithms.shared.PlaybackState
import com.thealgorithms.viz.AlgorithmSnapshot

@Composable
fun StatsPanel(
    algorithmName: String,
    snapshot: AlgorithmSnapshot,
    playbackState: PlaybackState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = algorithmName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Comparisons: ${snapshot.comparisons}  |  Swaps: ${snapshot.swaps}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column {
                Text(
                    text = "Elements: ${snapshot.arrayState.size}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "State: ${stateLabel(playbackState)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun stateLabel(state: PlaybackState): String = when (state) {
    is PlaybackState.Stopped -> "Stopped"
    is PlaybackState.Playing -> "Playing"
    is PlaybackState.Paused -> "Paused"
    is PlaybackState.Complete -> "Complete"
}
