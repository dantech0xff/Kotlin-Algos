# Phase 01: Foundation — Event API + Color Theme + Data Models

## Context
- Parent: [plan.md](./plan.md)
- Brainstorm: [brainstorm-260408-educational-excellence.md](../reports/brainstorm-260408-educational-excellence.md)
- No dependencies — this is the first phase

## Overview
- **Date**: 2026-04-08
- **Priority**: P1 (blocks everything)
- **Status**: Completed
- **Effort**: 2h

Lay the foundation: extend `AlgorithmEvent` with descriptions + pseudocode line refs, create centralized color theme, extend data models for rich algorithm metadata.

## Key Insights
- Events currently carry NO human-readable text — just structural data (indices)
- Colors duplicated inconsistently across SortVisualization, SearchVisualization, StatsPanel, NavigationPanel
- `AlgorithmInfo` is minimal: name + one-line description + `Any` visualizer
- `AlgorithmSnapshot` has no description or pseudocode highlight fields
- All event API changes MUST use default params for backward compat

## Requirements
- Add `description: String` (default `""`) to: `Compare`, `Swap`, `Pivot`, `Overwrite`, `Probe`, `Found`, `RangeCheck`, `Complete`
- Add `pseudocodeLine: Int?` (default `null`) to same events (except `Complete`)
- Create `VizColors.kt` — single source of truth for all colors
- Create `PseudocodeLine.kt` data model
- Create `Complexity.kt` data model
- Extend `AlgorithmInfo` with: difficulty, timeComplexity, spaceComplexity, isStable, tags, pseudocode
- Extend `AlgorithmSnapshot` with: `currentDescription: String`, `activePseudocodeLine: Int?`

## Architecture

### Event API (backward-compatible)
```kotlin
// algo-shared/AlgorithmEvent.kt
sealed interface AlgorithmEvent {
    data class Compare(
        val indices: Pair<Int, Int>,
        val description: String = "",
        val pseudocodeLine: Int? = null
    ) : AlgorithmEvent

    data class Swap(
        val indices: Pair<Int, Int>,
        val description: String = "",
        val pseudocodeLine: Int? = null
    ) : AlgorithmEvent

    data class Pivot(
        val index: Int,
        val description: String = "",
        val pseudocodeLine: Int? = null
    ) : AlgorithmEvent

    data class Overwrite(
        val index: Int,
        val newValue: Int,
        val description: String = "",
        val pseudocodeLine: Int? = null
    ) : AlgorithmEvent

    data class Probe(
        val index: Int,
        val description: String = "",
        val pseudocodeLine: Int? = null
    ) : AlgorithmEvent

    data class Found(
        val index: Int,
        val description: String = "",
        val pseudocodeLine: Int? = null
    ) : AlgorithmEvent

    data class RangeCheck(
        val low: Int,
        val high: Int,
        val description: String = "",
        val pseudocodeLine: Int? = null
    ) : AlgorithmEvent

    // Start, Select, Deselect, NotFound — unchanged
    // Complete gets description only (no pseudocodeLine):
    data class Complete(
        val result: List<Int>,
        val description: String = ""
    ) : AlgorithmEvent
}
```

### VizColors (centralized palette)
```kotlin
// algo-ui-shared/.../ui/theme/VizColors.kt
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

    // Misc
    val progressTrack = Color(0xFF33334A)
    val divider = Color(0xFF33334A)
}
```

### New Data Models
```kotlin
// algo-ui-shared/.../model/PseudocodeLine.kt
data class PseudocodeLine(
    val text: String,
    val indentLevel: Int = 0,
    val lineNumber: Int
)

// algo-ui-shared/.../model/Complexity.kt
data class Complexity(
    val best: String,
    val average: String,
    val worst: String
)

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }
```

### Extended AlgorithmInfo
```kotlin
data class AlgorithmInfo(
    val name: String,
    val category: AlgorithmCategory,
    val description: String,
    val visualizer: Any,
    // NEW:
    val difficulty: Difficulty = Difficulty.BEGINNER,
    val timeComplexity: Complexity = Complexity("—", "—", "—"),
    val spaceComplexity: String = "—",
    val isStable: Boolean = false,
    val tags: List<String> = emptyList(),
    val pseudocode: List<PseudocodeLine> = emptyList()
)
```

### Extended AlgorithmSnapshot
```kotlin
data class AlgorithmSnapshot(
    val arrayState: List<Int>,
    val highlights: Map<Int, HighlightReason> = emptyMap(),
    val comparisons: Int = 0,
    val swaps: Int = 0,
    val sortedIndices: Set<Int> = emptySet(),
    // NEW:
    val currentDescription: String = "",
    val activePseudocodeLine: Int? = null
)
```

## Related Code Files

### Modify
| File | Change |
|------|--------|
| `algo-shared/.../AlgorithmEvent.kt` | Add description + pseudocodeLine to 7 event types |
| `algo-ui-shared/.../model/AlgorithmInfo.kt` | Add 6 new fields with defaults |
| `algo-viz-engine/.../AlgorithmSnapshot.kt` | Add currentDescription + activePseudocodeLine |
| `algo-viz-engine/.../SnapshotReconstructor.kt` | Extract description/pseudocodeLine from events into snapshot |

### Create
| File | Purpose |
|------|---------|
| `algo-ui-shared/.../ui/theme/VizColors.kt` | Centralized color palette |
| `algo-ui-shared/.../model/PseudocodeLine.kt` | Pseudocode line data model |
| `algo-ui-shared/.../model/Complexity.kt` | Complexity + Difficulty models |

## Implementation Steps

1. **Extend `AlgorithmEvent.kt`** — Add `description: String = ""` and `pseudocodeLine: Int? = null` to Compare, Swap, Pivot, Overwrite, Probe, Found, RangeCheck. Default params ensure zero breakage.

2. **Create `PseudocodeLine.kt`** — In `algo-ui-shared/.../model/`

3. **Create `Complexity.kt`** — In `algo-ui-shared/.../model/` with Complexity data class + Difficulty enum

4. **Extend `AlgorithmInfo.kt`** — Add 6 new fields with safe defaults. Existing AlgorithmRegistry entries compile without changes.

5. **Extend `AlgorithmSnapshot.kt`** — Add `currentDescription: String = ""` and `activePseudocodeLine: Int? = null`

6. **Update `SnapshotReconstructor.kt`** — When processing events, extract description + pseudocodeLine and set them on the resulting snapshot. Handle events that carry these fields.

7. **Create `VizColors.kt`** — Consolidate all colors from SortVisualization, SearchVisualization, StatsPanel, NavigationPanel into single object.

8. **Run tests** — Verify all 147+ tests still pass. The default params ensure backward compat.

## Todo
- [ ] Extend AlgorithmEvent with description + pseudocodeLine (including Complete.description)
- [ ] Create PseudocodeLine.kt
- [ ] Create Complexity.kt + Difficulty enum
- [ ] Extend AlgorithmInfo with rich metadata fields
- [ ] Extend AlgorithmSnapshot with description + pseudocodeLine
- [ ] Update SnapshotReconstructor to propagate new fields
- [ ] Create VizColors.kt centralized palette
- [ ] Run all tests — verify zero breakage
- [ ] Test Desktop + Web (Wasm) builds both compile

## Success Criteria
- [ ] All existing tests pass without modification
- [ ] Events compile with description="" by default (backward compat)
- [ ] AlgorithmInfo has all new fields with safe defaults
- [ ] AlgorithmSnapshot carries description + pseudocodeLine
- [ ] VizColors.kt contains ALL colors used across the app
- [ ] SnapshotReconstructor propagates description from events to snapshots

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| Event API breaks existing algorithms | Default params = zero breakage |
| SnapshotReconstructor logic gets complex | Extract description only from events that carry it |
| Color mismatch during migration | VizColors values match existing hardcoded values exactly |

## Security Considerations
None — this is a client-side educational app with no network/auth concerns.

## Next Steps
- Phase 02 (Algorithm Descriptions) — uses new event API
- Phase 03 (Info Panels) — uses new data models + VizColors
