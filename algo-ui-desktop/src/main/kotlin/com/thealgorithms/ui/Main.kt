package com.thealgorithms.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.thealgorithms.ui.components.InputConfigPanel
import com.thealgorithms.ui.components.NavigationPanel
import com.thealgorithms.ui.components.PlaybackControls
import com.thealgorithms.ui.components.SearchVisualization
import com.thealgorithms.ui.components.SortVisualization
import com.thealgorithms.ui.components.StatsPanel
import com.thealgorithms.ui.model.AlgorithmCategory
import java.awt.Dimension

fun main() = application {
    val viewModel = remember { AppViewModel() }

    Window(
        onCloseRequest = {
            viewModel.destroy()
            exitApplication()
        },
        title = "Algorithm Visualizer",
        state = rememberWindowState(width = 1200.dp, height = 800.dp),
        onPreviewKeyEvent = { keyEvent ->
            when {
                keyEvent.type == KeyEventType.KeyDown -> {
                    when (keyEvent.key) {
                        Key(java.awt.event.KeyEvent.VK_SPACE) -> { viewModel.play(); true }
                        Key(java.awt.event.KeyEvent.VK_RIGHT) -> { viewModel.stepForward(); true }
                        Key(java.awt.event.KeyEvent.VK_LEFT) -> { viewModel.stepBack(); true }
                        Key(java.awt.event.KeyEvent.VK_R) -> { viewModel.stop(); true }
                        else -> false
                    }
                }
                else -> false
            }
        }
    ) {
        window.minimumSize = Dimension(1200, 800)
        MaterialTheme {
            AppContent(viewModel)
        }
    }
}

@Composable
fun AppContent(viewModel: AppViewModel) {
    val selectedAlgo by viewModel.selectedAlgorithm.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val snapshot by viewModel.snapshot.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        NavigationPanel(
            selectedAlgorithm = selectedAlgo,
            onAlgorithmSelected = { viewModel.selectAlgorithm(it) },
            modifier = Modifier.width(240.dp).fillMaxHeight()
        )

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            StatsPanel(
                algorithmName = selectedAlgo?.name ?: "Select an algorithm",
                snapshot = snapshot,
                playbackState = playbackState,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val algo = selectedAlgo
                if (algo != null) {
                    if (algo.category == AlgorithmCategory.SORTING) {
                        SortVisualization(snapshot, modifier = Modifier.fillMaxSize())
                    } else {
                        SearchVisualization(snapshot, modifier = Modifier.fillMaxSize())
                    }
                } else {
                    Text(
                        "Select an algorithm from the sidebar",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            PlaybackControls(
                playbackState = playbackState,
                progress = viewModel.progress.collectAsState().value,
                speedMs = viewModel.speedMs.collectAsState().value,
                onPlay = { viewModel.play() },
                onPause = { viewModel.pause() },
                onStop = { viewModel.stop() },
                onStepForward = { viewModel.stepForward() },
                onStepBack = { viewModel.stepBack() },
                onSpeedChange = { viewModel.setSpeed(it) },
                onRun = { viewModel.runAlgorithm() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            InputConfigPanel(
                inputArray = viewModel.inputArray.collectAsState().value,
                searchKey = viewModel.searchKey.collectAsState().value,
                isSearchAlgorithm = selectedAlgo?.category == AlgorithmCategory.SEARCHING,
                onInputChange = { viewModel.setInputArray(it) },
                onSearchKeyChange = { viewModel.setSearchKey(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
