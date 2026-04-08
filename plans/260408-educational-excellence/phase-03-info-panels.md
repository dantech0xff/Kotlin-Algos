# Phase 03: Info Panel Components

## Context
- Parent: [plan.md](./plan.md)
- Depends on: [phase-01-foundation.md](./phase-01-foundation.md) (data models + VizColors)
- Can run in parallel with: Phase 02
- Brainstorm: [brainstorm-260408-educational-excellence.md](../reports/brainstorm-260408-educational-excellence.md)

## Overview
- **Date**: 2026-04-08
- **Priority**: P1
- **Status**: Completed
- **Effort**: 3h

Build 5 new UI components for the right-column info panel: PseudocodePanel, StepExplanationPanel, ComplexityPanel, LegendPanel, and the container InfoPanel. Also migrate existing components to use VizColors.

## Key Insights
- Right panel = 280dp fixed width
- All components use VizColors (Phase 01) — no hardcoded colors
- PseudocodePanel needs active-line highlighting synced to current event
- StepExplanationPanel reads `snapshot.currentDescription`
- Components are pure stateless composables — data flows from AppViewModel

## Requirements
- PseudocodePanel: monospace, line numbers, highlighted active line
- StepExplanationPanel: human-readable text from snapshot description, auto-updates
- ComplexityPanel: table with best/avg/worst time, space, stability badge, difficulty stars, tag chips
- LegendPanel: horizontal row of colored dots + labels for all HighlightReasons
- InfoPanel: container that stacks the above vertically with dividers
- Migrate SortVisualization, SearchVisualization, StatsPanel, NavigationPanel to VizColors

## Architecture

### Component Hierarchy
```
InfoPanel (container, 280dp)
├── PseudocodePanel (pseudocode lines + active highlight)
├── Divider
├── StepExplanationPanel (current step description text)
├── Divider
├── ComplexityPanel (complexity table + stability + difficulty + tags)
├── Divider
└── LegendPanel (color meanings)
```

### PseudocodePanel
```
┌─────────────────────┐
│ 1 function bubble:  │  ← muted
│ 2   for i = 0..n:   │  ← muted
│ 3     for j = 0..n: │  ← muted
│ 4       if a[j]>..  │  ← HIGHLIGHTED (yellow bg)
│ 5         swap(..)  │  ← muted
│ 6   if no swaps:    │  ← muted
│ 7     break         │  ← muted
└─────────────────────┘
```
- Active line: `VizColors.pseudocodeActiveLine` background
- Line numbers: `VizColors.pseudocodeLineNumber`, right-aligned, 2-char width
- Indent: 16dp per indentLevel
- Font: Monospace, 13sp
- Smooth transition on highlight change (animateContentSize or animatedColor)

### StepExplanationPanel
```
┌─────────────────────┐
│ Step 5 of 28        │  ← muted counter
│                     │
│ Comparing arr[2]=7  │  ← primary text
│ with arr[3]=2.      │
│ Since 7 > 2, swap   │
│ positions 2 and 3.  │
└─────────────────────┘
```
- Text wraps, left-aligned
- Counter uses `VizColors.textMuted`
- Description uses `VizColors.textPrimary`
- Cross-fade animation between steps

### ComplexityPanel
```
┌─────────────────────┐
│ ⏱ Time Complexity   │
│ Best:    O(n)       │
│ Average: O(n²)      │
│ Worst:   O(n²)      │
│                     │
│ 💾 Space: O(1)      │
│ ✅ Stable: Yes      │
│ ⭐ Difficulty: ★★☆  │
│                     │
│ [comparison-based]  │  ← tag chips
│ [in-place]          │
└─────────────────────┘
```
- Table rows: label in muted, value in primary
- Stability: green ✓ or red ✗
- Difficulty: filled stars ★ + empty stars ☆
- Tags: small rounded chips with border

### LegendPanel
```
┌─────────────────────────────────────────┐
| 🟡 Compare  🔴 Swap  🟣 Pivot  🔵 Select |
| 🟠 Overwrite  🟢 Sorted  🔵 Range       |
└─────────────────────────────────────────┘
```
- 2-3 rows of colored circles (8dp) + label text (12sp)
- Maps `HighlightReason` → `VizColors` → label

### InfoPanel (Container)
```kotlin
@Composable
fun InfoPanel(
    algorithmInfo: AlgorithmInfo?,
    snapshot: AlgorithmSnapshot,
    playbackState: PlaybackState,
    progress: Pair<Int, Int>,
    modifier: Modifier = Modifier
)
```
- Scrollable Column
- Passes relevant data to child components
- Shows "Select an algorithm" placeholder when no algo selected

## Related Code Files

### Create
| File | Purpose |
|------|---------|
| `algo-ui-shared/.../ui/components/PseudocodePanel.kt` | Pseudocode with active-line highlighting |
| `algo-ui-shared/.../ui/components/StepExplanationPanel.kt` | Step narrative text |
| `algo-ui-shared/.../ui/components/ComplexityPanel.kt` | Complexity table + metadata |
| `algo-ui-shared/.../ui/components/LegendPanel.kt` | Color legend |
| `algo-ui-shared/.../ui/components/InfoPanel.kt` | Right column container |

### Modify (VizColors migration)
| File | Change |
|------|--------|
| `algo-ui-shared/.../ui/components/SortVisualization.kt` | Replace private color constants with VizColors |
| `algo-ui-shared/.../ui/components/SearchVisualization.kt` | Same |
| `algo-ui-shared/.../ui/components/StatsPanel.kt` | Same |
| `algo-ui-shared/.../ui/components/NavigationPanel.kt` | Same |
| `algo-ui-shared/.../ui/components/PlaybackControls.kt` | Same (if has colors) |

## Implementation Steps

1. **Create LegendPanel.kt** — Simplest component, good warm-up
2. **Create ComplexityPanel.kt** — Reads AlgorithmInfo metadata
3. **Create StepExplanationPanel.kt** — Reads snapshot.currentDescription
4. **Create PseudocodePanel.kt** — Most complex: line highlighting, indentation, monospace
5. **Create InfoPanel.kt** — Container composing all 4 + dividers
6. **Migrate SortVisualization.kt** to VizColors
7. **Migrate SearchVisualization.kt** to VizColors
8. **Migrate StatsPanel.kt** to VizColors
9. **Migrate NavigationPanel.kt** to VizColors
10. **Verify build** — All components compile, VizColors used everywhere

## Todo
- [ ] Create LegendPanel.kt
- [ ] Create ComplexityPanel.kt
- [ ] Create StepExplanationPanel.kt
- [ ] Create PseudocodePanel.kt
- [ ] Create InfoPanel.kt container
- [ ] Migrate SortVisualization to VizColors
- [ ] Migrate SearchVisualization to VizColors
- [ ] Migrate StatsPanel to VizColors
- [ ] Migrate NavigationPanel to VizColors
- [ ] Build verification

## Success Criteria
- [ ] All 5 new components compile and render
- [ ] PseudocodePanel highlights correct line based on snapshot.activePseudocodeLine
- [ ] StepExplanationPanel displays snapshot.currentDescription
- [ ] ComplexityPanel shows all metadata from AlgorithmInfo
- [ ] LegendPanel shows all 9 HighlightReason colors with labels
- [ ] Zero hardcoded color constants remain in SortVisualization, SearchVisualization, StatsPanel, NavigationPanel
- [ ] All existing tests pass

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| PseudocodePanel highlight animation janky | Test with Compose animation, use simple background color swap |
| 280dp too narrow for complexity table | Use compact layout, abbreviate if needed |
| Components look disconnected | InfoPanel provides consistent padding/dividers |

## Next Steps
- Phase 04 wires InfoPanel into three-column layout
