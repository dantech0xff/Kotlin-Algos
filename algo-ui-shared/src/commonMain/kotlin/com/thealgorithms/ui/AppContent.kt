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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thealgorithms.ui.components.InputConfigPanel
import com.thealgorithms.ui.components.NavigationPanel
import com.thealgorithms.ui.components.PlaybackControls
import com.thealgorithms.ui.components.SearchVisualization
import com.thealgorithms.ui.components.SortVisualization
import com.thealgorithms.ui.components.StatsPanel
import com.thealgorithms.ui.model.AlgorithmCategory
import com.thealgorithms.ui.model.VizKey

@Composable
fun AppContent(
    viewModel: AppViewModel,
    onKeyEvent: ((VizKey) -> Unit)? = null
) {
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
                algorithmDescription = selectedAlgo?.description,
                snapshot = snapshot,
                playbackState = playbackState,
                progress = viewModel.progress.collectAsState().value,
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
