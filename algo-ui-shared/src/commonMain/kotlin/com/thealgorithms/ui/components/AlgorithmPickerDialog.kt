package com.thealgorithms.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.thealgorithms.ui.model.AlgorithmInfo
import com.thealgorithms.ui.theme.VizColors

@Composable
fun AlgorithmPickerDialog(
    sortAlgorithms: List<AlgorithmInfo>,
    onConfirm: (List<AlgorithmInfo>) -> Unit,
    onDismiss: () -> Unit
) {
    val defaultNames = setOf("Bubble Sort", "Selection Sort", "Quick Sort", "Merge Sort")
    var selected by remember {
        mutableStateOf(sortAlgorithms.filter { it.name in defaultNames }.toMutableList())
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = VizColors.surfaceDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select 4 Algorithms to Compare",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VizColors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false).fillMaxWidth()
                ) {
                    items(sortAlgorithms) { algo ->
                        val isChecked = algo in selected
                        val isEnabled = isChecked || selected.size < 4
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isEnabled) {
                                    selected = if (algo in selected) {
                                        (selected - algo).toMutableList()
                                    } else if (selected.size < 4) {
                                        (selected + algo).toMutableList()
                                    } else {
                                        selected
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = null,
                                enabled = isEnabled,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = VizColors.textAccent,
                                    uncheckedColor = VizColors.textMuted
                                )
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = algo.name,
                                    color = VizColors.textPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = algo.description,
                                    color = VizColors.textMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = VizColors.divider)

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VizColors.progressTrack,
                            contentColor = VizColors.textPrimary
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { onConfirm(selected) },
                        enabled = selected.size == 4,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VizColors.textAccent,
                            contentColor = VizColors.textPrimary,
                            disabledContainerColor = VizColors.progressTrack,
                            disabledContentColor = VizColors.textMuted
                        )
                    ) {
                        Text("Start Compare")
                    }
                }
            }
        }
    }
}
