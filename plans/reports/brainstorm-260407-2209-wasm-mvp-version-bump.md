# Brainstorm: Wasm MVP — Version Bump + Wire Up

**Date:** 2026-04-07 22:09
**Status:** Agreed

## Problem Statement
Deploy existing Kotlin algorithm visualizer to web via Kotlin/Wasm. Current build fails with `skiko.mjs` module resolution error. Architecture is KMP-ready but versions are too old and upstream module deps are commented out.

## Root Cause
- Kotlin 1.9.23 + Compose 1.6.2 = wasmJs was Alpha. Skiko wasm artifacts not reliably resolved.
- Known issue: JetBrains/compose-multiplatform#4133
- No workaround exists at these versions. Must upgrade.

## Current State
- 6 modules: algo-shared, algo-core, algo-viz-engine, algo-ui-shared, algo-ui-desktop, algo-ui-web
- 8 algorithms (5 sort + 3 search), 130+ tests passing
- All Kotlin code already in `commonMain` source sets
- `algo-ui-web` is a placeholder shell — deps commented out, no real UI
- Desktop app works fully via `./gradlew :algo-ui-desktop:run`

## Evaluated Approaches

### A: Version Bump → Wire Up → Ship ✅ SELECTED
- Bump Kotlin 2.1.20, Compose 1.8.2, add compose-compiler plugin
- Add `wasmJs()` targets to 4 upstream modules
- Uncomment deps, replace placeholder with full AppContent
- ~1.5-2h effort, low risk

### B: Separate React/Kotlin.js Web Frontend ❌ REJECTED
- Violates DRY (duplicate UI), violates YAGNI (two stacks)
- 2-3x more work, harder to maintain
- Wastes existing algo-ui-shared extraction

### C: Wait for Compose Wasm to Stabilize ❌ REJECTED
- wasmJs is stable since Compose 1.7.0 (Oct 2024)
- No reason to wait at 1.8.2

## Agreed Solution: Approach A

### Version Targets
| Component | Current | Target |
|-----------|---------|--------|
| Kotlin | 1.9.23 | 2.1.20 |
| Compose Multiplatform | 1.6.2 | 1.8.2 |
| Compose Compiler Plugin | Missing | Required (Kotlin 2.0+) |
| Coroutines | 1.8.0 | 1.10.x |
| Kotest | 5.8.1 | 6.x (if KMP-compatible) |

### Files to Change
1. `gradle/libs.versions.toml` — version bumps + compose-compiler plugin
2. `build.gradle.kts` (root) — add compose-compiler plugin apply false
3. `algo-shared/build.gradle.kts` — add `wasmJs()` target
4. `algo-core/build.gradle.kts` — add `wasmJs()` target
5. `algo-viz-engine/build.gradle.kts` — add `wasmJs()` target
6. `algo-ui-shared/build.gradle.kts` — add `wasmJs()` target + compose-compiler
7. `algo-ui-web/build.gradle.kts` — uncomment deps, add compose-compiler, remove @OptIn
8. `algo-ui-web/.../Main.kt` — full AppContent integration
9. `algo-ui-web/src/wasmJsMain/resources/index.html` — proper HTML entry
10. `.github/workflows/deploy-web.yml` — GitHub Pages CI/CD

### Key Risks
- `Dispatchers.Default` in AlgorithmPlayer → wasmJs maps to JS main thread (acceptable, no blocking I/O)
- Kotest 6.x KMP compat needs verification (fallback: keep tests JVM-only)
- Binary size ~2-5MB (acceptable for educational tool)
- Kotlin 2.0+ may surface minor syntax warnings

### Success Criteria
- `./gradlew :algo-ui-web:wasmJsBrowserDistribution` succeeds
- `./gradlew :algo-ui-desktop:run` still works (no regression)
- All 130+ existing tests pass
- Web app shows full visualizer with algorithm selection, playback, visualization
- Deployed to GitHub Pages

### Post-MVP Migration Roadmap
1. Batch 1 (easy): HeapSort, CountingSort, RadixSort, ShellSort
2. Batch 2 (medium): JumpSearch, ExponentialSearch, InterpolationSearch
3. Batch 3 (hard): BFS, DFS, Dijkstra (new event types + graph canvas)

## Unresolved Questions
- Kotest 6.x KMP compatibility — may need to keep tests JVM-only initially
- Compose 1.8.2 exact compose-compiler version alignment
- `Dispatchers.Default` on wasmJs thread behavior under heavy animation
