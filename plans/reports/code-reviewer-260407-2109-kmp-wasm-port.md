## Code Review Summary

### Scope
- Files: `algo-ui-web/**`, `algo-ui-shared/**`, `algo-ui-desktop/**`, `algo-shared/**`, `algo-core/**`, `algo-viz-engine/**`, `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `.github/workflows/deploy-wasm.yml`
- LOC: ~1200 across 30+ files
- Focus: KMP Wasm port -- multi-module restructuring, shared UI extraction, CI/CD pipeline
- Scout findings: 2 critical, 4 high

### Overall Assessment
The restructuring is well-architected: clean module boundaries, proper source-set migration (`commonMain` for cross-platform code), good separation of concerns between `algo-ui-shared` and platform-specific entry points. However, there are two critical cross-platform compatibility bugs that will block the Wasm build at runtime or compile time, plus a CI pipeline that will fail due to missing memory configuration.

---

### Critical Issues

#### C1. `synchronized` in `RecordingEmitter` is not cross-platform
**File:** `algo-viz-engine/src/commonMain/kotlin/com/thealgorithms/viz/AlgorithmPlayer.kt:36`
```kotlin
synchronized(_recorded) { _recorded.add(value) }
```
`kotlin.synchronized` is JVM-only. It does not exist in Kotlin/JS or Kotlin/Wasm. Since `RecordingEmitter` lives in `commonMain`, this code will fail to compile for any non-JVM target. When the wasmJs target is added to `algo-viz-engine` (required for the full integration described in `algo-ui-web/build.gradle.kts` TODO), compilation will break.
**Impact:** Blocks adding `wasmJs()` target to `algo-viz-engine`, which blocks the full Wasm integration.
**Fix:** Use `kotlinx.atomicfu` or restructure to avoid shared mutable state (the `RecordingEmitter` is only used during `suspend fun run()` which is single-threaded per invocation -- the `synchronized` may be unnecessary).

#### C2. `Dispatchers.Main` unavailable in commonMain without platform-specific dependency
**File:** `algo-ui-shared/src/commonMain/kotlin/com/thealgorithms/ui/AppViewModel.kt:28`
```kotlin
private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
```
`Dispatchers.Main` resolves to `MissingMainDispatcherError` at runtime if no platform-specific dispatcher module is on the classpath. For JVM desktop, `compose.desktop.currentOs` pulls in `kotlinx-coroutines-swing` transitively -- so desktop works. But:
- `algo-ui-shared` only depends on `coroutines-core` (which does NOT provide `Main` dispatcher)
- When `algo-ui-web` eventually depends on `algo-ui-shared`, Wasm does not have a `Dispatchers.Main` implementation unless `kotlinx-coroutines-core` is properly configured for Wasm (available from coroutines 1.8+ but requires the wasmJs variant)
- The current placeholder Wasm shell does not use `AppViewModel` so this does not manifest today, but it will break when full integration is attempted
**Impact:** Runtime crash when Wasm entry point tries to instantiate `AppViewModel`.
**Fix:** Either add `expect fun provideMainDispatcher(): CoroutineDispatcher` with `actual` per platform, or inject the scope from the platform entry point rather than creating it inside the ViewModel.

#### C3. CI workflow will fail -- Wasm compiler OOM without memory config
**File:** `.github/workflows/deploy-wasm.yml` and `gradle.properties`
Running `./gradlew :algo-ui-web:wasmJsBrowserDistribution` locally fails:
```
Not enough memory to run compilation. Try to increase it via 'gradle.properties':
    kotlin.daemon.jvmargs=-Xmx<size>
```
`gradle.properties` only contains `org.jetbrains.compose.experimental.wasm.enabled=true` -- no JVM heap or Kotlin daemon args. The GitHub Actions runner (ubuntu-latest, 7GB RAM) will also hit this.
**Impact:** CI pipeline will fail on every push to master matching the path filters.
**Fix:** Add to `gradle.properties`:
```
org.gradle.jvmargs=-Xmx2g
kotlin.daemon.jvmargs=-Xmx2g
```

---

### High Priority

#### H1. `AlgorithmPlayer` creates an unmanaged `CoroutineScope(Dispatchers.Default)` -- double scope leak
**File:** `algo-viz-engine/src/commonMain/kotlin/com/thealgorithms/viz/AlgorithmPlayer.kt:43`
`AlgorithmPlayer` creates its own `CoroutineScope(Dispatchers.Default + SupervisorJob())`. `AppViewModel` also creates its own `CoroutineScope(Dispatchers.Main + SupervisorJob())`. When `AppViewModel.destroy()` calls `player.destroy()`, the player's scope is cancelled. But if `destroy()` is never called (e.g., process killed), both scopes leak. More importantly, `AppViewModel` launches work on its own scope AND the player launches playback on its own scope -- two independent scopes for one logical unit of work.
**Impact:** Coroutine leaks and potential race conditions if both scopes launch concurrent operations on shared state.
**Fix:** Pass the ViewModel's scope into `AlgorithmPlayer` rather than having the player create its own.

#### H2. `AlgorithmInfo.visualizer: Any` loses type safety
**File:** `algo-ui-shared/src/commonMain/kotlin/com/thealgorithms/ui/model/AlgorithmInfo.kt:7`
```kotlin
data class AlgorithmInfo(
    val name: String,
    val category: AlgorithmCategory,
    val description: String,
    val visualizer: Any
)
```
Then in `AlgorithmRegistry.kt:31-32`:
```kotlin
fun visualizerAsSort(info: AlgorithmInfo): VisualizableAlgorithm {
    return info.visualizer as VisualizableAlgorithm
}
```
Using `Any` with unchecked `as` casts will produce `ClassCastException` at runtime if the registry data is ever inconsistent (e.g., during refactoring). This is a type-safety gap that the compiler cannot catch.
**Impact:** Silent `ClassCastException` if category/visualizer mismatch.
**Fix:** Use a sealed interface for the visualizer type, or make `AlgorithmInfo` generic, or at minimum use a `sealed class AlgorithmVisualizer` wrapper.

#### H3. `InputConfigPanel.parseArray` silently drops non-positive integers
**File:** `algo-ui-shared/src/commonMain/kotlin/com/thealgorithms/ui/components/InputConfigPanel.kt:104`
```kotlin
private fun parseArray(text: String): List<Int> {
    return text.split(",", " ", ";")
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it > 0 }
}
```
The `filter { it > 0 }` silently discards zero and negative integers. If a user enters `0, -5, 3`, they get `[3]` with no feedback. For search algorithms specifically, searching for value `0` in an array containing `0` is a valid test case.
**Impact:** Silent data loss; confusing UX when input appears to be accepted but elements vanish.
**Fix:** Either remove the filter (allow any integer) or show a validation message when non-positive values are entered.

#### H4. `SearchVisualization` cell count heuristic produces variable layouts
**File:** `algo-ui-shared/src/commonMain/kotlin/com/thealgorithms/ui/components/SearchVisualization.kt:26`
```kotlin
val cellCount = (snapshot.arrayState.size / 8).coerceIn(1, 6)
```
For an array of size 5, `cellCount = 5/8 = 0` coerced to 1. For size 48, `cellCount = 6`. For size 8, `cellCount = 1`. The grid columns change dramatically with small input changes (size 7 -> 1 column, size 9 -> 1 column, size 16 -> 2 columns). This creates jarring layout jumps when the user changes input.
**Impact:** Jarring UX with inconsistent grid layouts; not a correctness bug but a usability issue.

---

### Medium Priority

#### M1. `EventBuffer` silently drops old events with `removeAt(0)` -- index corruption
**File:** `algo-viz-engine/src/commonMain/kotlin/com/thealgorithms/viz/EventBuffer.kt:9-11`
```kotlin
if (events.size >= maxCapacity) {
    events.removeAt(0)
}
events.add(event)
```
When `maxCapacity` is reached, old events are silently discarded, but `AlgorithmPlayer` indexes into the buffer by absolute position (`buffer.get(it)` at line 186). After events are dropped from index 0, all snapshot indexes become stale -- the snapshot at index 50 now refers to what is at buffer position 0, causing incorrect state reconstruction.
**Impact:** Silent data corruption when algorithm emits >10,000 events. Likely rare for small arrays but will manifest with large inputs.
**Fix:** Either use a ring buffer with offset tracking, or throw an exception when capacity is exceeded rather than silently dropping.

#### M2. `stateLabel` duplicated between `PlaybackControls` and `StatsPanel`
**Files:**
- `algo-ui-shared/src/commonMain/kotlin/com/thealgorithms/ui/components/PlaybackControls.kt:134-139`
- `algo-ui-shared/src/commonMain/kotlin/com/thealgorithms/ui/components/StatsPanel.kt:61-66`

Two private `stateLabel` functions exist with slightly different formatting. DRY violation.
**Impact:** Maintenance burden; strings may diverge.
**Fix:** Extract to a shared utility.

#### M3. `PlaybackControls` slider passes raw `Long` -- no input validation
**File:** `algo-ui-shared/src/commonMain/kotlin/com/thealgorithms/ui/components/PlaybackControls.kt:120`
```kotlin
onValueChange = { sliderPos = it; onSpeedChange(it.toLong()) },
```
The slider's `valueRange = 10f..2000f` constrains the UI, but `onSpeedChange` passes the float-to-long conversion directly. `AppViewModel.setSpeed` re-clamps via `coerceIn(10L, 2000L)`, so there's double clamping. Not a bug, but redundant.
**Impact:** Minor code smell.

#### M4. `NavigationPanel` uses identity comparison for selection
**File:** `algo-ui-shared/src/commonMain/kotlin/com/thealgorithms/ui/components/NavigationPanel.kt:49`
```kotlin
isSelected = selectedAlgorithm?.name == algo.name,
```
Uses `name` string comparison rather than object identity. `AlgorithmInfo` is a `data class`, so structural equality would work, but comparing by `name` is fragile if two algorithms ever share a name.
**Impact:** Low risk given current data.

---

### Low Priority

#### L1. Hardcoded colors in `SearchVisualization`
**File:** `algo-ui-shared/src/commonMain/kotlin/com/thealgorithms/ui/components/SearchVisualization.kt:72-78`
Colors like `0xFFBBDEFB`, `0xFFA5D6A7`, `0xFFFFE082` are hardcoded rather than using Material theme tokens. Similarly in `SortVisualization.kt:12-13` and `NavigationPanel.kt:30` (`Color(0xFFF5F5F5)`).
**Impact:** Does not respect dark theme; will look wrong in dark mode.

#### L2. `SortVisualization` corner radius threshold is magic number
**File:** `algo-ui-shared/src/commonMain/kotlin/com/thealgorithms/ui/components/SortVisualization.kt:22`
```kotlin
val cornerRadius = if (snapshot.arrayState.size > 25) 0f else 4f
```
Magic threshold. Should be a named constant.

#### L3. `gradle.properties` missing `org.gradle.configuration-cache=true` or `org.gradle.caching=true`
No build caching enabled. For CI, this would speed up builds significantly.

---

### Edge Cases Found by Scout

1. **`Dispatchers.Main` in commonMain code** -- Only works on JVM with `coroutines-swing` on classpath. Will crash on Wasm/JS.
2. **`synchronized` in commonMain** -- JVM-only intrinsic; compile-time failure for wasmJs target.
3. **`EventBuffer.removeAt(0)` on ArrayList** -- O(n) per removal; with 10k events this becomes quadratic. Should use `ArrayDeque` if ring buffer is desired.
4. **`SearchAlgorithmAdapter` ignores `input` parameter** -- `VisualizableAlgorithm.execute(input, emitter)` receives `input: List<Int>` but the adapter uses `searchInput` captured at construction time, discarding the passed `input`. This works because `AppViewModel.runAlgorithm()` passes the same array, but it's a latent bug if anyone calls `player.run(adapter, differentArray)`.
5. **No CORS headers in index.html** -- The Wasm app loads `algo-viz.js` from the same origin, so CORS is not an issue for Pages deployment. However, if the app fetches external APIs in the future, the HTML has no CSP headers.
6. **`resolveCellColors` highlight logic conflates range size > 3 with "range highlight"** -- If a binary search happens to highlight exactly 4 cells (low..high range > 3), all cells turn blue regardless of which specific indices are highlighted. The heuristic may miscolor for certain algorithm states.

---

### Positive Observations

1. **Clean module boundary** -- `algo-ui-shared` properly extracts all shared UI into `commonMain` with no JVM imports. Desktop `Main.kt` is correctly slim (44 lines, entry point only).
2. **Well-structured KMP source sets** -- `algo-shared`, `algo-core`, `algo-viz-engine` all moved to `commonMain` with tests in `jvmTest`. No `jvmMain` leakage.
3. **Desktop duplicate cleanup is complete** -- No leftover files in `algo-ui-desktop/src/main/kotlin/com/thealgorithms/ui/` beyond `Main.kt`. All components, models, and `AppViewModel` properly deleted.
4. **CI workflow has correct structure** -- Proper permissions scoping (`contents: read`, `pages: write`, `id-token: write`), concurrency group to prevent parallel deployments, `workflow_dispatch` for manual trigger, path filters limited to relevant modules.
5. **VizKey enum** -- Clean cross-platform abstraction for keyboard input, avoiding AWT `KeyEvent` in shared code.
6. **Build compiles and tests pass** -- Verified `algo-ui-desktop:compileKotlin`, `algo-ui-shared:compileKotlinJvm`, `algo-ui-web:compileKotlinWasmJs` all succeed. Core and engine tests pass.
7. **`AppViewModel.destroy()` is called** on desktop window close, preventing coroutine leaks.

---

### Recommended Actions

1. **[Critical/Blocking]** Fix `synchronized` in `RecordingEmitter` -- replace with atomic or remove (single-coroutine usage).
2. **[Critical/Blocking]** Add `gradle.properties` memory configuration before merging CI workflow.
3. **[High]** Address `Dispatchers.Main` in `AppViewModel` -- either inject scope or use `expect/actual`.
4. **[High]** Replace `AlgorithmInfo.visualizer: Any` with typed sealed interface.
5. **[High]** Fix `InputConfigPanel.parseArray` to accept zero and negative integers or show validation.
6. **[Medium]** Fix `EventBuffer` index corruption when capacity exceeded.
7. **[Medium]** Extract duplicate `stateLabel` to shared utility.
8. **[Low]** Replace hardcoded colors with Material theme tokens.

---

### Metrics
- Type Coverage: ~85% (the `Any`-typed `visualizer` field is the main gap)
- Test Coverage: Tests exist for `algo-core` and `algo-viz-engine`; no tests for `algo-ui-shared` UI components
- Linting Issues: 0 syntax errors; build compiles
- Cross-platform Readiness: **Not ready** -- `synchronized` and `Dispatchers.Main` block non-JVM targets

### Unresolved Questions

1. What Kotlin/Compose version is targeted for Wasm stability? Kotlin 1.9.23 + Compose 1.6.2 use `ExperimentalWasmDsl` -- is there a timeline for stable Wasm support that would inform when to remove the `@OptIn`?
2. Is `algo-ui-web` intended to ship as a placeholder (current state) or should the full integration be completed before merging? The TODO items suggest this is incremental, but the CI workflow will deploy the placeholder to GitHub Pages.
3. Should `EventBuffer.maxCapacity` be configurable or documented? 10,000 is a reasonable default but not documented anywhere.
4. Is there a plan for testing `algo-ui-shared` components? Currently no test source set exists for the shared UI module.
