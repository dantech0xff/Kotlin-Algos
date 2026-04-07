package com.thealgorithms.ui

import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.thealgorithms.ui.model.VizKey
import java.awt.Dimension
import java.awt.event.KeyEvent

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
                        Key(KeyEvent.VK_SPACE) -> { viewModel.onKey(VizKey.PLAY_PAUSE); true }
                        Key(KeyEvent.VK_RIGHT) -> { viewModel.onKey(VizKey.STEP_FORWARD); true }
                        Key(KeyEvent.VK_LEFT) -> { viewModel.onKey(VizKey.STEP_BACK); true }
                        Key(KeyEvent.VK_R) -> { viewModel.onKey(VizKey.RESET); true }
                        else -> false
                    }
                }
                else -> false
            }
        }
    ) {
        window.minimumSize = Dimension(1200, 800)
        AppContent(viewModel = viewModel)
    }
}
