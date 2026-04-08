package com.thealgorithms.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.thealgorithms.ui.components.AlgorithmPickerDialog
import com.thealgorithms.ui.components.ComparePanel
import com.thealgorithms.ui.components.InfoPanel
import com.thealgorithms.ui.components.InputConfigPanel
import com.thealgorithms.ui.components.LegendPanel
import com.thealgorithms.ui.components.NavigationPanel
import com.thealgorithms.ui.components.PlaybackControls
import com.thealgorithms.ui.components.SearchVisualization
import com.thealgorithms.ui.components.SortVisualization
import com.thealgorithms.ui.components.StatsPanel
import com.thealgorithms.ui.model.AlgorithmCategory
import com.thealgorithms.ui.model.AlgorithmRegistry
import com.thealgorithms.ui.model.VizKey
import com.thealgorithms.ui.model.ViewMode
import com.thealgorithms.ui.theme.VizColors

@Composable
fun AppContent(
    viewModel: AppViewModel,
    onKeyEvent: ((VizKey) -> Unit)? = null
) {
    val selectedAlgo by viewModel.selectedAlgorithm.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val snapshot by viewModel.snapshot.collectAsState()

    var viewMode by remember { mutableStateOf(ViewMode.SINGLE) }
    var compareViewModel by remember { mutableStateOf<CompareViewModel?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    var showSidebar by remember { mutableStateOf(true) }

    // Responsive: detect width
    var containerWidthDp by remember { mutableStateOf(800.dp) }
    val density = LocalDensity.current
    val isCompact = containerWidthDp < 600.dp

    // Clean up CompareViewModel when composable leaves composition
    DisposableEffect(compareViewModel) {
        onDispose { compareViewModel?.destroy() }
    }

    Box(modifier = Modifier.fillMaxSize().onSizeChanged { size ->
        containerWidthDp = with(density) { size.width.toDp() }
    }) {
        if (viewMode == ViewMode.COMPARE) {
            // Compare mode: full width, no sidebar
            val cvm = compareViewModel
            if (cvm != null) {
                ComparePanel(
                    viewModel = cvm,
                    onBack = {
                        cvm.destroy()
                        compareViewModel = null
                        viewMode = ViewMode.SINGLE
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Single algorithm mode
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar: always visible on wide screens, drawer on compact
                val showNav = !isCompact || showSidebar
                if (showNav) {
                    NavigationPanel(
                        selectedAlgorithm = selectedAlgo,
                        onAlgorithmSelected = { viewModel.selectAlgorithm(it) },
                        onCompareClicked = { showPicker = true },
                        isCompareMode = viewMode == ViewMode.COMPARE,
                        modifier = Modifier.width(if (isCompact) 200.dp else 200.dp).fillMaxHeight()
                    )
                    if (isCompact) {
                        // Click-away overlay to close sidebar on mobile
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .clickable { showSidebar = false }
                        )
                    }
                }

                // Main content
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(
                        start = if (isCompact) 8.dp else 16.dp,
                        end = if (isCompact) 8.dp else 16.dp,
                        top = if (isCompact) 4.dp else 16.dp,
                        bottom = if (isCompact) 4.dp else 16.dp
                    )
                ) {
                    // Mobile top bar with hamburger menu
                    if (isCompact) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            IconButton(onClick = { showSidebar = true }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = VizColors.textPrimary
                                )
                            }
                            Text(
                                text = selectedAlgo?.name ?: "Select algorithm",
                                style = MaterialTheme.typography.titleSmall,
                                color = VizColors.textPrimary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    StatsPanel(
                        algorithmName = selectedAlgo?.name ?: "Select an algorithm",
                        algorithmDescription = selectedAlgo?.description,
                        snapshot = snapshot,
                        playbackState = playbackState,
                        progress = viewModel.progress.collectAsState().value,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(if (isCompact) 4.dp else 8.dp))

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

                    Spacer(Modifier.height(if (isCompact) 4.dp else 8.dp))

                    LegendPanel(modifier = Modifier.fillMaxWidth())

                    Spacer(Modifier.height(if (isCompact) 2.dp else 8.dp))

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
                        onSeek = { viewModel.seekTo(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(if (isCompact) 2.dp else 8.dp))

                    InputConfigPanel(
                        inputArray = viewModel.inputArray.collectAsState().value,
                        searchKey = viewModel.searchKey.collectAsState().value,
                        isSearchAlgorithm = selectedAlgo?.category == AlgorithmCategory.SEARCHING,
                        onInputChange = { viewModel.setInputArray(it) },
                        onSearchKeyChange = { viewModel.setSearchKey(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Right: Info panel — only on wide screens
                if (!isCompact && selectedAlgo != null) {
                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight().width(1.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    InfoPanel(
                        algorithmInfo = selectedAlgo,
                        snapshot = snapshot,
                        playbackState = playbackState,
                        progress = viewModel.progress.collectAsState().value,
                        modifier = Modifier.width(280.dp).fillMaxHeight()
                    )
                }
            }
        }
    }

    // Picker dialog overlay
    if (showPicker) {
        AlgorithmPickerDialog(
            sortAlgorithms = AlgorithmRegistry.sortAlgorithms,
            onConfirm = { selected ->
                val cvm = CompareViewModel()
                cvm.selectAlgorithms(selected)
                compareViewModel = cvm
                viewMode = ViewMode.COMPARE
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}
