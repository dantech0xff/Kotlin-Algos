package com.thealgorithms.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thealgorithms.shared.PlaybackState

@Composable
fun PlaybackControls(
    playbackState: PlaybackState,
    progress: Pair<Int, Int>,
    speedMs: Long,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onStepForward: () -> Unit,
    onStepBack: () -> Unit,
    onSpeedChange: (Long) -> Unit,
    onRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step Back
            IconButton(onClick = onStepBack, enabled = progress.first > 0) {
                Icon(Icons.Default.FastRewind, contentDescription = "Step Back")
            }

            // Play / Pause
            if (playbackState is PlaybackState.Playing) {
                IconButton(onClick = onPause) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                }
            } else {
                IconButton(
                    onClick = onPlay,
                    enabled = progress.second > 0
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                }
            }

            // Stop
            IconButton(onClick = onStop, enabled = playbackState !is PlaybackState.Stopped) {
                Icon(Icons.Default.Stop, contentDescription = "Stop")
            }

            // Step Forward
            IconButton(
                onClick = onStepForward,
                enabled = progress.second > 0 && progress.first < progress.second - 1
            ) {
                Icon(Icons.Default.FastForward, contentDescription = "Step Forward")
            }

            // Run button
            Button(
                onClick = onRun,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Run")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Event ${progress.first + 1} / ${progress.second.coerceAtLeast(1)}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = stateLabel(playbackState),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fast", style = MaterialTheme.typography.labelSmall)
            var sliderPos by remember(speedMs) { mutableStateOf(speedMs.toFloat()) }
            Slider(
                value = sliderPos,
                onValueChange = { sliderPos = it; onSpeedChange(it.toLong()) },
                valueRange = 10f..2000f,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Text("Slow", style = MaterialTheme.typography.labelSmall)
            Text(
                text = "${speedMs}ms",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.width(60.dp)
            )
        }
    }
}

private fun stateLabel(state: PlaybackState): String = when (state) {
    is PlaybackState.Stopped -> "Stopped"
    is PlaybackState.Playing -> "Playing"
    is PlaybackState.Paused -> "Paused"
    is PlaybackState.Complete -> "Complete (${state.totalEvents} events)"
}
