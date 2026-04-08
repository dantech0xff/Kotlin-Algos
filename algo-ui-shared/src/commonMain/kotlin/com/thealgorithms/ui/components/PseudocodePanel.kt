package com.thealgorithms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thealgorithms.ui.model.PseudocodeLine
import com.thealgorithms.ui.theme.VizColors

@Composable
fun PseudocodePanel(
    pseudocode: List<PseudocodeLine>,
    activeLine: Int?,
    modifier: Modifier = Modifier
) {
    if (pseudocode.isEmpty()) return

    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        items(pseudocode, key = { it.lineNumber }) { line ->
            val isActive = line.lineNumber == activeLine
            val bgColor = if (isActive) VizColors.pseudocodeActiveLine else VizColors.surfaceDark

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor, RoundedCornerShape(4.dp))
                    .padding(vertical = 3.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${line.lineNumber}",
                    color = VizColors.pseudocodeLineNumber,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                // Indentation
                Spacer(Modifier.width((line.indentLevel * 16).dp))
                Text(
                    text = line.text,
                    color = if (isActive) VizColors.textPrimary else VizColors.textMuted,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
