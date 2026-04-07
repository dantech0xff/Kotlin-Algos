---
title: "KMP Wasm Port — Browser + Desktop Algorithm Visualizer"
description: "Restructure existing modules into Kotlin Multiplatform with Wasm target for browser deployment"
status: complete
priority: P1
effort: 5h
branch: master
tags: [kmp, wasm, compose-multiplatform, github-pages]
created: 2026-04-07
---

# KMP Wasm Port — Browser + Desktop Algorithm Visualizer

## Goal
Port existing Compose Desktop app to Kotlin Multiplatform (KMP) with Wasm browser target. One shared codebase → Desktop + Web.

## Current State
- 4 modules: algo-shared, algo-core, algo-viz-engine, algo-ui-desktop
- Kotlin 1.9.23, Compose 1.6.2, JVM toolchain 20
- 8 algorithms visualized, 130+ tests passing

## Target State
- 6 modules: + algo-ui-shared (shared Compose UI), + algo-ui-web (Wasm entry)
- Kotlin 1.9.23 (current — Wasm already supported), Compose 1.6.2 (current — Wasm Canvas OK)
- `commonMain` source sets for algo-shared, algo-core, algo-viz-engine
- Shared Compose UI in algo-ui-shared `commonMain`
- Desktop entry in algo-ui-desktop `jvmMain`
- Web entry in algo-ui-web `wasmJsMain`
- GitHub Pages auto-deploy via Actions

## Dependency Graph
```
Phase 01 (Version Upgrade)
    ↓
Phase 02 (Restructure core modules → commonMain)
    ↓
Phase 03 (Extract shared UI → algo-ui-shared)
    ↓
Phase 04 (Wasm entry → algo-ui-web) + Phase 05 (Desktop update → algo-ui-desktop)
    ↓
Phase 06 (GitHub Pages CI/CD)
```

## Phase Summary

| Phase | File | Scope | Est. | Status |
|-------|------|-------|------|--------|
| 01 | [phase-01-version-upgrade.md](phase-01-version-upgrade.md) | Kotlin 2.0+, Compose 1.7+, KMP plugins | 0.5h | [x] |
| 02 | [phase-02-restructure-core.md](phase-02-restructure-core.md) | Move source to commonMain, multiplatform build files | 1h | [x] |
| 03 | [phase-03-shared-ui.md](phase-03-shared-ui.md) | Extract Compose UI to algo-ui-shared commonMain | 1.5h | [x] |
| 04 | [phase-04-wasm-entry.md](phase-04-wasm-entry.md) | Wasm browser entry, index.html, keyboard handling | 1h | [x] |
| 05 | [phase-05-desktop-update.md](phase-05-desktop-update.md) | Desktop entry slim-down, use shared UI | 0.5h | [x] |
| 06 | [phase-06-github-pages.md](phase-06-github-pages.md) | GitHub Actions, auto-deploy to Pages | 0.5h | [x] |

## Key Architecture Decisions
- **No version upgrade needed** — Kotlin 1.9.23 + Compose 1.6.2 already support Wasm (confirmed by research)
- Phase 01 simplified: only add KMP plugins, no version bumps
- **6-module structure**: existing 4 + algo-ui-shared (shared Compose) + algo-ui-web (Wasm)
- **Source layout**: `commonMain` for shared code, `jvmMain`/`wasmJsMain` for platform-specific
- **Tests stay in `commonTest`** for core modules (cross-platform testing)
- **GitHub Pages** via `wasmJsBrowserDistribution` task + Actions deploy

## Risk Assessment
- Kotlin 2.0 migration may surface code changes (language syntax evolution)
- Compose 1.7 may have API changes from 1.6.2
- JVM toolchain 20 vs 21 — Wasm target needs no specific JVM version at runtime
- Binary size ~2-5MB for Kotlin/Wasm output

## Rollback
Each phase independently revertible via git revert.

## Unresolved Questions
- Kotest KMP compatibility — Kotest 5.x supports multiplatform but may need config tweaks
- Canvas drawing behavior identical on Wasm? (research says yes, needs verification)
