package com.thealgorithms.ui.web

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.CanvasBasedWindow

/**
 * Wasm browser entry point for the Algorithm Visualizer.
 *
 * This is a minimal shell that proves the Compose Wasm pipeline compiles.
 * Full integration with shared UI (AppContent, AppViewModel) requires
 * upstream modules (algo-shared, algo-core, algo-viz-engine, algo-ui-shared)
 * to publish wasmJs variants. See build.gradle.kts for the TODO items.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "root", title = "Algorithm Visualizer") {
        MaterialTheme {
            PlaceholderApp()
        }
    }
}

@Composable
private fun PlaceholderApp() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Algorithm Visualizer — Kotlin/Wasm")
            Text("Full UI coming soon (pending upstream wasmJs targets)")
        }
    }
}
