package com.thealgorithms.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thealgorithms.shared.PlaybackState
import com.thealgorithms.ui.CompareViewModel
import com.thealgorithms.ui.theme.VizColors

@Composable
fun ComparePanel(
    viewModel: CompareViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slots by viewModel.slots.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isReady by viewModel.isReady.collectAsState()
    val speedMs by viewModel.speedMs.collectAsState()
    val inputArray by viewModel.inputArray.collectAsState()

    // Derive progress from slots
    val minIndex = slots.minOfOrNull { it.eventIndex } ?: 0
    val maxTotal = slots.maxOfOrNull { it.totalEvents } ?: 0
    val progress = minIndex to maxTotal

    val playbackState: PlaybackState = when {
        isPlaying -> PlaybackState.Playing
        slots.isNotEmpty() && slots.all { it.isComplete } -> PlaybackState.Complete(maxTotal)
        isReady -> PlaybackState.Paused  // Events loaded & ready → Play enabled
        else -> PlaybackState.Stopped
    }

    Column(modifier = modifier.padding(8.dp)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Compare Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VizColors.textPrimary,
                fontSize = 18.sp
            )
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VizColors.progressTrack,
                    contentColor = VizColors.textPrimary
                )
            ) {
                Text("← Back to Single", fontSize = 12.sp)
            }
        }

        // 2x2 grid
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    slots.getOrNull(0)?.let { slot ->
                        CompareSlot(
                            slotState = slot,
                            slotIndex = 0,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    slots.getOrNull(1)?.let { slot ->
                        CompareSlot(
                            slotState = slot,
                            slotIndex = 1,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    slots.getOrNull(2)?.let { slot ->
                        CompareSlot(
                            slotState = slot,
                            slotIndex = 2,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    slots.getOrNull(3)?.let { slot ->
                        CompareSlot(
                            slotState = slot,
                            slotIndex = 3,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Shared playback controls
        PlaybackControls(
            playbackState = playbackState,
            progress = progress,
            speedMs = speedMs,
            onPlay = { viewModel.play() },
            onPause = { viewModel.pause() },
            onStop = { viewModel.stop() },
            onStepForward = { viewModel.stepForward() },
            onStepBack = { viewModel.stepBack() },
            onSpeedChange = { viewModel.setSpeed(it) },
            onRun = { viewModel.runAll() },
            onSeek = {},
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        // Shared input config
        InputConfigPanel(
            inputArray = inputArray,
            searchKey = 0,
            isSearchAlgorithm = false,
            onInputChange = { viewModel.setInputArray(it) },
            onSearchKeyChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
