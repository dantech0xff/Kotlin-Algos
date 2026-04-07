---
title: "Wasm MVP: Version Bump → Wire Up → Ship"
description: "Upgrade Kotlin 2.1.20 + Compose 1.8.2, add wasmJs() targets to all modules, wire algo-ui-web, deploy to GitHub Pages"
status: complete
priority: P1
effort: 3h
branch: main
tags: [wasm, compose-multiplatform, version-bump, deploy]
created: 2026-04-07
---

## Overview

Fix the `Can't resolve './skiko.mjs'` blocker by upgrading Kotlin 1.9.23→2.1.20 and Compose 1.6.2→1.8.2. Wire the existing shared UI into algo-ui-web. Deploy to GitHub Pages.

## Phase Summary

| # | Phase | Effort | Key Changes | Blocks |
|---|-------|--------|-------------|--------|
| 1 | [Version Bump](phase-01-version-bump.md) | 30m | `libs.versions.toml`, root `build.gradle.kts`, compose-compiler plugin | Everything |
| 2 | [Add wasmJs Targets](phase-02-add-wasm-targets.md) | 30m | Add `wasmJs()` to 4 upstream modules | Phase 1 |
| 3 | [Wire Web Module](phase-03-wire-web-module.md) | 1h | Uncomment deps, rewrite Main.kt, update index.html, update gradle.properties | Phase 2 |
| 4 | [Verify & Deploy](phase-04-verify-deploy.md) | 1h | Run all tests, verify desktop, verify wasm build, fix deploy-wasm.yml | Phase 3 |

## Dependency Graph

```
Phase 1 (version bump)
  └─▶ Phase 2 (wasmJs targets)
       └─▶ Phase 3 (wire web module)
            └─▶ Phase 4 (verify + deploy)
```

All phases are strictly sequential. No parallelism possible.

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| API breakage from Kotlin 2.0+ | Medium | Low | All code is in commonMain; syntax changes rare |
| Kotest 5.8.1 incompat with K2 | Low | Low | Tests are JVM-only via `jvmTest`; keep as-is |
| Skiko wasm artifact still missing | Low | Critical | Compose 1.8.2 ships stable wasm; verified in issue tracker |
| Dispatchers.Default on wasmJs | Certain | Low | Maps to JS main thread; acceptable for viz app |
| Binary size >5MB | Low | Low | Acceptable; can optimize later with dead code elimination |

## Success Criteria

1. `./gradlew :algo-ui-web:wasmJsBrowserDistribution` — succeeds, produces distributable
2. `./gradlew :algo-ui-desktop:run` — still works (no regression)
3. All `algo-core:jvmTest` + `algo-viz-engine:jvmTest` pass (130+ tests)
4. Web app renders full visualizer with algorithm selection, playback, visualization
5. GitHub Pages deployment succeeds on push to master

## Unresolved Questions

- Kotest 6.x KMP compat unverified — plan assumes keeping tests JVM-only in `jvmTest` source sets
- `org.jetbrains.compose.experimental.wasm.enabled=true` in gradle.properties — may be removable at Compose 1.8.2 (no longer experimental); verify during Phase 4
