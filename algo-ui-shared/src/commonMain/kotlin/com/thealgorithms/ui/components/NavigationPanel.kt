package com.thealgorithms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thealgorithms.ui.model.AlgorithmInfo
import com.thealgorithms.ui.model.AlgorithmRegistry
import com.thealgorithms.ui.theme.VizColors

@Composable
fun NavigationPanel(
    selectedAlgorithm: AlgorithmInfo?,
    onAlgorithmSelected: (AlgorithmInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterText by remember { mutableStateOf("") }

    Column(modifier = modifier.background(VizColors.sidebarBackground)) {
        Text(
            text = "Algorithms",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = VizColors.textPrimary,
            modifier = Modifier.padding(16.dp)
        )

        // Search filter
        OutlinedTextField(
            value = filterText,
            onValueChange = { filterText = it },
            placeholder = { Text("Search...", style = MaterialTheme.typography.bodySmall, color = VizColors.textMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = VizColors.textPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VizColors.sidebarAccentBorder,
                unfocusedBorderColor = VizColors.divider,
                cursorColor = VizColors.textAccent
            ),
            trailingIcon = {
                if (filterText.isNotEmpty()) {
                    IconButton(
                        onClick = { filterText = "" },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = VizColors.textMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        )

        HorizontalDivider(color = VizColors.divider)

        val filteredSort = AlgorithmRegistry.sortAlgorithms.filter { it.name.contains(filterText, ignoreCase = true) }
        val filteredSearch = AlgorithmRegistry.searchAlgorithms.filter { it.name.contains(filterText, ignoreCase = true) }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (filteredSort.isNotEmpty()) {
                item {
                    SectionHeader("📊 Sorting")
                }
            }
            items(filteredSort) { algo ->
                AlgorithmItem(
                    info = algo,
                    isSelected = selectedAlgorithm?.name == algo.name,
                    onClick = { onAlgorithmSelected(algo) }
                )
            }
            if (filteredSearch.isNotEmpty()) {
                item {
                    SectionHeader("🔍 Searching")
                }
            }
            items(filteredSearch) { algo ->
                AlgorithmItem(
                    info = algo,
                    isSelected = selectedAlgorithm?.name == algo.name,
                    onClick = { onAlgorithmSelected(algo) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun AlgorithmItem(
    info: AlgorithmInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val bgColor = when {
        isSelected -> VizColors.sidebarSelectedBg
        isHovered -> VizColors.sidebarHoverBg
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
            .clickable(onClick = onClick)
            .background(bgColor)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        isHovered = when (event.type) {
                            androidx.compose.ui.input.pointer.PointerEventType.Enter -> true
                            androidx.compose.ui.input.pointer.PointerEventType.Exit -> false
                            else -> isHovered
                        }
                    }
                }
            }
    ) {
        // Left accent border for selected item
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .height(36.dp)
                    .background(VizColors.sidebarAccentBorder, RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Text(
                text = info.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else VizColors.textPrimary.copy(alpha = 0.85f)
            )
            Text(
                text = info.description,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = VizColors.textMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
