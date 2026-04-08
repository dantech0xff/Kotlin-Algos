package com.thealgorithms.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thealgorithms.ui.theme.VizColors
import kotlin.random.Random

@Composable
fun InputConfigPanel(
    inputArray: List<Int>,
    searchKey: Int,
    isSearchAlgorithm: Boolean,
    onInputChange: (List<Int>) -> Unit,
    onSearchKeyChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var arrayText by remember(inputArray) {
        mutableStateOf(inputArray.joinToString(", "))
    }
    var keyText by remember(searchKey) { mutableStateOf(searchKey.toString()) }
    var sizeText by remember { mutableStateOf("10") }

    Column(modifier = modifier.padding(vertical = 4.dp)) {
        // Preset buttons row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Presets:", style = MaterialTheme.typography.labelSmall, color = VizColors.textMuted)
            listOf("Random", "Nearly Sorted", "Reversed", "All Equal", "Duplicates").forEach { label ->
                Button(
                    onClick = {
                        val size = sizeText.toIntOrNull()?.coerceIn(5, 50) ?: 10
                        val array = generatePreset(label, size)
                        arrayText = array.joinToString(", ")
                        onInputChange(array)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VizColors.progressTrack,
                        contentColor = VizColors.textPrimary
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = arrayText,
                onValueChange = { arrayText = it },
                label = { Text("Array (comma-separated)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = sizeText,
                onValueChange = { sizeText = it },
                label = { Text("Size") },
                modifier = Modifier.width(80.dp),
                singleLine = true
            )

            Button(onClick = {
                val size = sizeText.toIntOrNull()?.coerceIn(5, 50) ?: 10
                val randomArray = List(size) { Random.nextInt(1, 100) }
                arrayText = randomArray.joinToString(", ")
                onInputChange(randomArray)
            }) {
                Text("Random")
            }

            Button(onClick = {
                val parsed = parseArray(arrayText)
                if (parsed.isNotEmpty()) onInputChange(parsed)
            }) {
                Text("Apply")
            }
        }

        if (isSearchAlgorithm) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("Search Key") },
                    modifier = Modifier.width(160.dp),
                    singleLine = true
                )
                Button(onClick = {
                    val key = keyText.toIntOrNull() ?: 0
                    onSearchKeyChange(key)
                }) {
                    Text("Set Key")
                }
            }
        }
    }
}

private fun parseArray(text: String): List<Int> {
    return text.split(",", " ", ";")
        .mapNotNull { it.trim().toIntOrNull() }
}

private fun generatePreset(label: String, size: Int): List<Int> = when (label) {
    "Random" -> List(size) { Random.nextInt(1, 100) }
    "Nearly Sorted" -> {
        val arr = (1..size).toMutableList()
        // Perform 1-2 random swaps to make it nearly sorted
        if (size >= 2) {
            val swaps = Random.nextInt(1, 3)
            repeat(swaps) {
                val i = Random.nextInt(size)
                val j = Random.nextInt(size)
                val tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp
            }
        }
        arr
    }
    "Reversed" -> (size downTo 1).toList()
    "All Equal" -> List(size) { 42 }
    "Duplicates" -> List(size) { listOf(1, 3, 5, 7, 9).random() }
    else -> List(size) { Random.nextInt(1, 100) }
}
