---
title: "Phase 3: Error Handling + ComparePanel Update"
description: "Add error StateFlow to CompareViewModel, show error banner in ComparePanel, simplify playbackState"
status: pending
priority: P1
effort: 1h
phase: 3 of 3
---

# Phase 3: Error Handling + ComparePanel Update

## Context

After Phase 2 rewrite, need error handling for algorithm execution failures. ComparePanel needs minor updates to consume new reactive API and display errors.

## Overview

- Add `_error` StateFlow to CompareViewModel
- Wrap `runAndPrepare()` calls in try/catch
- Add error banner to ComparePanel
- Simplify playbackState derivation in ComparePanel

## Files

| File | Action |
|------|--------|
| `algo-ui-shared/.../CompareViewModel.kt` | MODIFY |
| `algo-ui-shared/.../ComparePanel.kt` | MODIFY |

## Implementation Steps

### Step 1: Add error StateFlow to CompareViewModel

```kotlin
private val _error = MutableStateFlow<String?>(null)
val error: StateFlow<String?> = _error.asStateFlow()

fun clearError() {
    _error.value = null
}
```

- Place near other StateFlow declarations
- Exposed as read-only StateFlow

### Step 2: Wrap runAll() in try/catch

```kotlin
fun runAll() {
    scope.launch {
        _error.value = null
        _isReady.value = false
        val selected = _selectedAlgorithms.value
        if (selected.size != 4) {
            _error.value = "Select exactly 4 algorithms"
            return@launch
        }
        try {
            selected.forEachIndexed { i, algoInfo ->
                val player = players[i]
                val algorithm = algorithmFactory.create(algoInfo.id)
                val input = inputGenerator.generate(algoInfo.id)
                try {
                    player.runAndPrepare(algorithm, input)
                } catch (e: Exception) {
                    _error.value = "Failed to run ${algoInfo.name}: ${e.message}"
                    return@launch
                }
                yield()
            }
            _isReady.value = true
        } catch (e: Exception) {
            _error.value = "Unexpected error: ${e.message}"
        }
    }
}
```

- Outer try/catch for unexpected errors
- Inner try/catch per-player with specific error message
- Early return on failure (don't continue with broken state)

### Step 3: Update ComparePanel — error banner

In `ComparePanel.kt`, add error banner at top of column:

```kotlin
// Inside Composable, near top of layout
val error by viewModel.error.collectAsState()

if (error != null) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.clearError() }) {
                Icon(Icons.Default.Close, "Dismiss")
            }
        }
    }
}
```

- Dismissible error banner
- Uses MaterialTheme error colors
- Above the 2x2 grid

### Step 4: Simplify playbackState in ComparePanel

Replace any manual playback state derivation with collection of `viewModel.playbackState`:

```kotlin
val playbackState by viewModel.playbackState.collectAsState()
```

- No local computation needed
- Reactive `playbackState` from Phase 2 handles aggregation

### Step 5: Verify CompareSlot integration

`CompareSlot` receives `CompareSlotState` — verify it reads:
- `playbackState` → for visual indicator
- `eventIndex` / `totalEvents` → for progress
- `snapshot` → for rendering

All these now come from reactive `slots` StateFlow. No changes needed in CompareSlot unless it has local state caching.

## Success Criteria

- [ ] Algorithm execution errors show in error banner
- [ ] Error banner is dismissable via X button
- [ ] Error clears when re-running
- [ ] Playback state derived from `viewModel.playbackState` (no local computation)
- [ ] CompareSlot renders correctly with reactive data
- [ ] No compilation errors

## Risk Assessment

| Risk | Likelihood | Mitigation |
|------|-----------|------------|
| Missing import for Icons.Default.Close | Low | Compose Material Icons dependency already present |
| Error container colors not themed | Low | MaterialTheme.colorScheme provides these |
| CompareSlot expects old data format | Low | CompareSlotState shape unchanged |
