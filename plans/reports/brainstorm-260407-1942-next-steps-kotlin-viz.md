# Brainstorm: Kotlin Visualization App - Next Steps

**Date:** 2026-04-07
**Status:** Agreed

## Current State
- MVP: 8 algorithms (5 sort + 3 search) visualized in Compose Desktop
- ~130 tests, all passing
- 4 Gradle modules: algo-shared, algo-core, algo-viz-engine, algo-ui-desktop
- Remaining Java algorithms: 52 sorts + 32 searches (+ other categories)

## Agreed Direction

**Order: Wasm → Migrate → Polish**

### Phase A: KMP Wasm Port (Priority 1)

**Goal:** One shared Compose Multiplatform codebase → Desktop + Web (Wasm)

**Approach:** Shared KMP
- Restructure `algo-shared`, `algo-core`, `algo-viz-engine` as multiplatform modules (`commonMain`)
- Split UI into shared (visualization canvas, playback controls) + platform (window, navigation)
- Keep Desktop working throughout migration

**Key changes:**
- `build.gradle.kts`: add `kotlin("multiplatform")` + `org.jetbrains.compose` multiplatform config
- Source layout: `src/commonMain/kotlin/` (shared), `src/jvmMain/kotlin/` (desktop), `src/wasmJsMain/kotlin/` (web)
- Remove JVM-only deps (Swing/AWT) from shared code
- Add Wasm entry point: `algo-ui-web/src/wasmJsMain/kotlin/.../Main.kt`
- `index.html` + wasm bundling for web

**Risks:**
- Compose Wasm is still evolving (1.6+ stable but some gaps)
- Canvas API differences between JVM/Wasm (minor)
- `java.awt` imports must be isolated to JVM-only code

**Hosting:** GitHub Pages via GitHub Actions auto-deploy

**Estimated effort:** 3-4h

### Phase B: Expand to ~20 Algorithms (Priority 2)

**Goal:** Add ~12 more algorithms to the visualizer

**Candidates (high value, good visualization):**
| Algorithm | Category | Why |
|-----------|----------|-----|
| HeapSort | Sort | Heap visualization is educational |
| TimSort | Sort | Real-world (Python/Java default) |
| CountingSort | Sort | Non-comparison sort, different pattern |
| RadixSort | Sort | LSD/MSD approach, visually distinct |
| ShellSort | Sort | Gap sequence visualization |
| BFS | Graph | Level-by-level exploration |
| DFS | Graph | Depth-first path tracing |
| Dijkstra | Graph | Shortest path, classic |
| InterpolationSearch | Search | Different from binary, formula-based |
| JumpSearch | Search | Block-based, visually interesting |
| ExponentialSearch | Search | Double-then-binary pattern |
| TernarySearch | Search | Three-way split |

**Note:** Graph algorithms need new event types (`Visit`, `Explore`, `Backtrack`, `PathUpdate`) and a graph visualization component (node+edge canvas). This is a bigger lift than sort/search.

**Suggested split:**
1. Batch 1 (easy, same pattern): 4 more sorts → 9 total sorts
2. Batch 2 (medium, new events): 3 more searches → 6 total searches
3. Batch 3 (hard, new viz): 4 graph algorithms + graph canvas

**Estimated effort:** 4-6h

### Phase C: Polish (Priority 3)

**Goal:** Ship-quality desktop + web app

**Features:**
- Distributable packaging (.dmg/.exe/.deb) via `compose.desktop.jPackage`
- Dark/light theme toggle with Material 3 dynamic theming
- Smooth bar animations (animateFloatAsState for bar heights)
- Algorithm complexity info card (time/space complexity, best/worst case)
- Export visualization as GIF/MP4
- Sound effects on compare/swap/complete (optional, fun)
- Responsive layout for web (mobile-friendly)
- PWA manifest for offline web usage

**Estimated effort:** 3-4h

## Total Estimated Effort: 10-14h

## Architecture Decision: KMP Restructuring

```
algo-shared/       → commonMain (interfaces, events, PlaybackState)
algo-core/         → commonMain (algorithms, utils)
algo-viz-engine/   → commonMain (player, snapshots, playback)
algo-ui-shared/    → NEW - commonMain (shared Compose UI: canvas, controls, panels)
algo-ui-desktop/   → jvmMain (Desktop window, keyboard shortcuts)
algo-ui-web/       → NEW - wasmJsMain (browser entry, index.html)
```

**Why `algo-ui-shared`:** Canvas rendering, playback controls, stats panel are identical across Desktop/Web. Only window management and platform APIs differ.

## Unresolved Questions
- Graph algorithm visualization: node-edge canvas is significantly more complex than bar chart. Defer to post-batch-2?
- Compose Wasm binary size: Kotlin/Wasm produces ~2-5MB. Acceptable?
- CI/CD: GitHub Actions for auto-deploy to GitHub Pages needs Wasm build + Pages deployment step
