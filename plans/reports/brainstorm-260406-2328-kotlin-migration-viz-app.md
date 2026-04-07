# Brainstorm: Java → Kotlin Migration + Algorithm Visualization App

**Date:** 2026-04-06
**Status:** Agreed — proceed to implementation planning

---

## Problem Statement

Migrate Java algorithms repo (1,542 files, 27 categories) to Kotlin and build a Compose Desktop visualization app. Target: portfolio project with impressive UI.

## Current State

- **Language:** Java 21 (no Kotlin files despite repo name)
- **Build:** Maven, JUnit 5 + AssertJ tests (742 test files)
- **Categories:** 27 topics, user selected 4 priority ones
- **No existing UI or visualization**

## Selected Categories (299 files total)

| Category | Files | Visualization Potential |
|----------|-------|------------------------|
| Sorting | 52 | High — bar chart animations, color-coded comparisons |
| Search + Graph | 50 | High — tree/graph traversal, path highlighting |
| Dynamic Programming | 55 | Medium — table fill animations, memo visualization |
| Data Structures | 142 | High — insert/delete/search animations on trees, heaps, hash maps |

## Agreed Architecture

### Visualization Engine: Streaming Pattern
- Algorithms emit events via Kotlin Flow/SharedFlow
- UI collects and renders in real-time
- Buffer events for step-back playback capability
- Speed control via coroutine delay manipulation

### Project Structure: Multi-module Gradle
```
Kotlin-Algos/
├── algo-core/           # Pure Kotlin algorithms
├── algo-viz-engine/     # Streaming viz engine (events, state, playback)
├── algo-ui-desktop/     # Compose Desktop app
├── algo-shared/         # Shared models, interfaces
└── build.gradle.kts     # Root build file
```

### Migration Strategy: Auto-convert + Refactor
- IntelliJ Java-to-Kotlin converter for initial conversion
- Manual refactor to idiomatic Kotlin (data classes, extension functions, coroutines)
- Keep existing Java tests as reference, write new Kotlin tests

### UI Features (All 3 types)
1. **Step-by-step animation** — visual element highlighting, transitions
2. **Code + Output demo** — syntax-highlighted Kotlin code view with execution pointer
3. **Interactive playground** — custom input, speed control, pause/resume, step forward/back

## Phased Approach

### Phase 1 — Foundation (MVP)
- Setup multi-module Gradle + Compose Desktop
- Implement visualization engine core (event system, playback controller)
- Migrate + visualize **5-10 algorithms** from Sorting + Search categories
- Validate architecture end-to-end

### Phase 2 — Expand
- Migrate full categories: Sorting → Search → DP → Data Structures
- Polish UI: animations, controls, code view, input configuration
- Add algorithm metadata (description, complexity, references)

### Phase 3 — Polish
- Refactor all converted code to idiomatic Kotlin
- Documentation, README, screenshots, demo GIFs
- GitHub Actions CI/CD
- Performance optimization

## Key Technical Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Viz pattern | Streaming (Flow) | Real-time, flexible, Kotlin-native |
| UI framework | Compose Desktop | Cross-platform, modern, Kotlin-first |
| Project structure | Multi-module | Clean separation, testable |
| Migration | Auto-convert + refactor | Speed + quality balance |
| Scope | 4 categories first | Focus > breadth for portfolio |

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Scope too large | Project stall | Phase strictly, MVP first with 5-10 algos |
| Streaming step-back complexity | Architecture mess | Buffer events, hybrid snapshot-stream |
| Algorithm instrumentation tedious | Slow progress | Template pattern, code generation helpers |
| Auto-convert produces Java-ish Kotlin | Poor code quality | Dedicated refactor phase per category |
| Compose Desktop learning curve | UI quality | Start simple, iterate |

## Success Criteria

- [ ] 5+ algorithms with full visualization in Phase 1
- [ ] Clean multi-module architecture
- [ ] Interactive controls (play/pause/step/speed)
- [ ] Idiomatic Kotlin code
- [ ] README with demo screenshots/GIFs
- [ ] Build passes, tests pass

## Unresolved Questions

- None at this stage
