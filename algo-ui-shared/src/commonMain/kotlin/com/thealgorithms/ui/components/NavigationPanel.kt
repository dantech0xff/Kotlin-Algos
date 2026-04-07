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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thealgorithms.ui.model.AlgorithmInfo
import com.thealgorithms.ui.model.AlgorithmRegistry

private val SidebarBackground = Color(0xFF1E1E2E)
private val SidebarTextPrimary = Color(0xFFF8F8F2)
private val SidebarTextMuted = Color(0xFF8888A8)
private val SidebarSelectedBg = Color(0xFF2A2A40)
private val SidebarAccentBorder = Color(0xFF7C7CF8)
private val SidebarHoverBg = Color(0xFF252540)

@Composable
fun NavigationPanel(
    selectedAlgorithm: AlgorithmInfo?,
    onAlgorithmSelected: (AlgorithmInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(SidebarBackground)) {
        Text(
            text = "Algorithms",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SidebarTextPrimary,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider(color = Color(0xFF33334A))
        LazyColumn(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                SectionHeader("📊 Sorting")
            }
            items(AlgorithmRegistry.sortAlgorithms) { algo ->
                AlgorithmItem(
                    info = algo,
                    isSelected = selectedAlgorithm?.name == algo.name,
                    onClick = { onAlgorithmSelected(algo) }
                )
            }
            item {
                SectionHeader("🔍 Searching")
            }
            items(AlgorithmRegistry.searchAlgorithms) { algo ->
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
    val bgColor = if (isSelected) SidebarSelectedBg else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
            .clickable(onClick = onClick)
            .background(bgColor)
    ) {
        // Left accent border for selected item
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .height(36.dp)
                    .background(SidebarAccentBorder, RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
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
                color = if (isSelected) Color.White else SidebarTextPrimary.copy(alpha = 0.85f)
            )
            Text(
                text = info.description,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = SidebarTextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
