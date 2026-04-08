# Phase 03: Integration + Mode Toggle

## Context
- Parent: [plan.md](./plan.md)
- Depends on: [phase-02-ui-components.md](./phase-02-ui-components.md)

## Overview
- **Date**: 2026-04-08
- **Priority**: P1
- **Status**: Pending
- **Effort**: 2h

Wire CompareViewModel and ComparePanel into AppContent. Add "Compare" button to sidebar. Add mode switching logic.

## Key Insights
- AppContent currently has one layout: Nav + Center(Single) + Right(Info)
- Need to switch center between single-mode and compare-mode based on ViewMode
- CompareViewModel is separate from AppViewModel — they don't share state
- Sidebar "Compare" button triggers AlgorithmPickerDialog → on confirm → switch to COMPARE mode
- "Back to Single" in ComparePanel switches back to SINGLE mode
- NavigationPanel needs to show which mode is active

## Requirements
- ViewMode state in AppContent (or a top-level holder)
- Mode switch: SINGLE → COMPARE with algorithm picker
- Mode switch: COMPARE → SINGLE with "Back" button
- Sidebar "Compare" button visible only in SINGLE mode
- Single algorithm view completely unchanged when in SINGLE mode

## Architecture

### AppContent Changes
```kotlin
@Composable
fun AppContent(
    viewModel: AppViewModel,
    onKeyEvent: ((VizKey) -> Unit)? = null
) {
    var viewMode by remember { mutableStateOf(ViewMode.SINGLE) }
    var compareViewModel by remember { mutableStateOf<CompareViewModel?>(null) }
    var showPicker by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize()) {
        NavigationPanel(
            selectedAlgorithm = if (viewMode == ViewMode.SINGLE) viewModel.selectedAlgorithm.collectAsState().value else null,
            onAlgorithmSelected = { viewModel.selectAlgorithm(it) },
            onCompareClicked = { showPicker = true },  // NEW
            isCompareMode = viewMode == ViewMode.COMPARE,  // NEW
            modifier = Modifier.width(200.dp).fillMaxHeight()
        )

        if (viewMode == ViewMode.SINGLE) {
            // ... existing single-algorithm layout unchanged ...
        } else {
            ComparePanel(
                slots = compareViewModel?.slots?.collectAsState()?.value ?: emptyList(),
                // ... all other params ...
                onBack = {
                    compareViewModel?.destroy()
                    compareViewModel = null
                    viewMode = ViewMode.SINGLE
                }
            )
        }
    }

    if (showPicker) {
        AlgorithmPickerDialog(
            sortAlgorithms = AlgorithmRegistry.sortAlgorithms,
            preselected = listOf(/* defaults: Bubble, Selection, Quick, Merge */),
            onConfirm = { selected ->
                val cvm = CompareViewModel()
                cvm.selectAlgorithms(selected)
                compareViewModel = cvm
                viewMode = ViewMode.COMPARE
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}
```

### NavigationPanel Changes
Add parameters:
- `onCompareClicked: () -> Unit` — callback when "Compare" button pressed
- `isCompareMode: Boolean` — highlight the button when in compare mode

Add button at top of sidebar:
```kotlin
// Above algorithm list
Button(
    onClick = onCompareClicked,
    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = if (isCompareMode) VizColors.textAccent else VizColors.sidebarSelectedBg
    )
) {
    Text("⚡ Compare", color = VizColors.textPrimary)
}
```

## Related Code Files

### Modify
| File | Change |
|------|--------|
| `algo-ui-shared/.../ui/AppContent.kt` | Add ViewMode state, mode switching, ComparePanel integration |
| `algo-ui-shared/.../ui/components/NavigationPanel.kt` | Add "Compare" button + params |

## Implementation Steps

1. Update NavigationPanel — add `onCompareClicked`, `isCompareMode` params + button
2. Update AppContent — add ViewMode state, conditional layout, AlgorithmPickerDialog
3. Wire CompareViewModel lifecycle (create on enter, destroy on exit)
4. Build verify

## Todo
- [ ] Update NavigationPanel with Compare button
- [ ] Update AppContent with ViewMode switching
- [ ] Wire CompareViewModel lifecycle
- [ ] Build verify: `./gradlew :algo-ui-shared:compileKotlinWasmJs :algo-ui-desktop:compileKotlin`

## Success Criteria
- [ ] "Compare" button visible in sidebar during single mode
- [ ] Clicking "Compare" opens AlgorithmPickerDialog
- [ ] Confirming 4 algorithms switches to 2×2 compare grid
- [ ] "Back to Single" returns to normal single-algorithm view
- [ ] Single-algorithm view completely unchanged
- [ ] Both Desktop + Web compile

## Next Steps
- Phase 04: Testing + visual polish
