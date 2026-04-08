package com.thealgorithms.ui.theme

import androidx.compose.ui.graphics.Color

object VizColors {
    // Surfaces
    val surfaceDark = Color(0xFF1E1E2E)
    val canvasDark = Color(0xFF1A1A2E)
    val gridDark = Color(0xFF1E1E32)
    val cellDefault = Color(0xFF2D2D44)
    val cellBorder = Color(0xFF4A4A6A)

    // Semantic highlights
    val comparing = Color(0xFFFFD93D)
    val swapping = Color(0xFFFF6B6B)
    val pivoting = Color(0xFFA855F7)
    val selecting = Color(0xFF60A5FA)
    val overwriting = Color(0xFFF97316)
    val sorted = Color(0xFF22C55E)
    val found = Color(0xFF22C55E)
    val probing = Color(0xFFFFD93D)
    val rangeHighlight = Color(0xFF818CF8)
    val defaultElement = Color(0xFF4ECDC4)
    val keyDot = Color(0xFF38BDF8)

    // Text
    val textPrimary = Color(0xFFF8F8F2)
    val textMuted = Color(0xFF8888A8)
    val textAccent = Color(0xFF7C7CF8)

    // Sidebar
    val sidebarBackground = Color(0xFF1E1E2E)
    val sidebarSelectedBg = Color(0xFF2A2A40)
    val sidebarHoverBg = Color(0xFF252540)
    val sidebarAccentBorder = Color(0xFF7C7CF8)

    // Stats
    val chipComparisons = Color(0xFF60A5FA)
    val chipSwaps = Color(0xFFF472B6)
    val chipElements = Color(0xFF34D399)

    // Pseudocode
    val pseudocodeActiveLine = Color(0xFF2A2A40)
    val pseudocodeLineNumber = Color(0xFF6A6A8A)

    // Compare mode
    val compareSlotColors = listOf(
        Color(0xFF60A5FA), // blue
        Color(0xFFA855F7), // purple
        Color(0xFF22C55E), // green
        Color(0xFFF97316), // orange
    )

    // Misc
    val progressTrack = Color(0xFF33334A)
    val divider = Color(0xFF33334A)
}
