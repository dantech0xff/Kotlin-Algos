package com.thealgorithms.ui.web

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.thealgorithms.ui.AppContent
import com.thealgorithms.ui.AppViewModel

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "root", title = "Algorithm Visualizer") {
        MaterialTheme {
            AppContent(viewModel = AppViewModel())
        }
    }
}
