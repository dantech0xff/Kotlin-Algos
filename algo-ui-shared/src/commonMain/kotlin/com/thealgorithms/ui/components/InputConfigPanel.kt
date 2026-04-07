package com.thealgorithms.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
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
        .filter { it > 0 }
}
