# Phase 07: Testing & Polish — Comprehensive Tests, README, Demo, CI/CD

**Date:** 2026-04-07
**Priority:** P2
**Status:** [ ] Pending

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: Phase 04 (sorting), Phase 05 (search), Phase 06 (desktop UI)
- Research: [researcher-02-kotlin-migration-patterns.md](research/researcher-02-kotlin-migration-patterns.md) — Kotest patterns

## Overview

Add end-to-end integration tests, update project documentation, capture demo screenshots, and verify the full build pipeline works cleanly. This phase validates everything built in Phases 01-06.

## Key Insights

- Unit tests already exist per algorithm (Phase 04/05). This phase adds integration + E2E.
- Integration test: `AlgorithmPlayer` + real algorithm → verify correct event sequences
- E2E test: Compose Desktop test framework (`compose.uiTest`) for UI interactions
- No CI/CD pipeline needed for MVP — just verify `./gradlew check` passes
- README updates document the new Kotlin modules alongside existing Java code

## Requirements

### Functional
- Integration tests: player + each algorithm produces complete event sequence
- UI smoke test: app launches without crash (Compose Desktop test rule)
- Performance test: BubbleSort on 200 elements completes in < 5 seconds
- README section for the Kotlin visualization app
- Demo screenshot or GIF for the README

### Non-Functional
- Total test execution < 60 seconds
- No flaky tests
- Clean `./gradlew check` with zero warnings (or documented suppressions)

## Architecture

### Test Pyramid

```
       ┌─────────────┐
       │   E2E Tests  │  (Phase 07) — app launch, basic interaction
       │     (3-5)    │
       ├─────────────┤
       │ Integration  │  (Phase 07) — player + algorithm event sequences
       │   Tests (8)  │
       ├─────────────┤
       │  Unit Tests  │  (Phase 04/05) — algorithm correctness
       │   (50+)      │
       └─────────────┘
```

## Related Code Files

### Create
- `algo-viz-engine/src/commonTest/kotlin/com/thealgorithms/viz/integration/BubbleSortIntegrationTest.kt`
- `algo-viz-engine/src/commonTest/kotlin/com/thealgorithms/viz/integration/BinarySearchIntegrationTest.kt`
- `algo-viz-engine/src/commonTest/kotlin/com/thealgorithms/viz/integration/QuickSortIntegrationTest.kt`
- `algo-viz-engine/src/commonTest/kotlin/com/thealgorithms/viz/integration/MergeSortIntegrationTest.kt`
- `algo-viz-engine/src/commonTest/kotlin/com/thealgorithms/viz/integration/AllAlgorithmsIntegrationTest.kt` (parameterized)
- `algo-viz-engine/src/commonTest/kotlin/com/thealgorithms/viz/performance/PerformanceTest.kt`
- `plans/260406-2328-kotlin-migration-viz-app/demo/` (screenshots)

### Modify
- `README.md` — add Kotlin Visualization App section
- `algo-ui-desktop/build.gradle.kts` — add Compose test dependencies (if needed)

### Delete
- None

## Implementation Steps

1. Create integration test for BubbleSort + AlgorithmPlayer: run full sort, verify event sequence and final snapshot
2. Create integration test for BinarySearch + AlgorithmPlayer: verify probe/range events
3. Create parameterized `AllAlgorithmsIntegrationTest` — runs each algorithm through player, verifies `Start` + `Complete` events
4. Create `PerformanceTest` — BubbleSort on 200 elements, assert completion time < 5s
5. Run all tests: `./gradlew check` — fix any failures
6. Launch app manually, capture screenshot for demo
7. Update `README.md` with:
   - "Kotlin Visualization App" section with screenshot
   - How to run: `./gradlew :algo-ui-desktop:run`
   - Module structure overview
   - MVP algorithm list
8. Final verification: clean build + all tests pass on fresh clone

## Todo List

- [ ] Create BubbleSort integration test
- [ ] Create BinarySearch integration test
- [ ] Create AllAlgorithms parameterized integration test
- [ ] Create PerformanceTest
- [ ] Run `./gradlew check` and fix failures
- [ ] Capture demo screenshot
- [ ] Update README.md
- [ ] Final clean build verification

## Success Criteria

- [ ] `./gradlew check` passes across all modules (zero test failures)
- [ ] All 8 algorithms pass integration tests (player produces complete event sequences)
- [ ] Performance test passes: BubbleSort(200) < 5s
- [ ] README updated with app section and run instructions
- [ ] Demo screenshot captured and linked in README
- [ ] Clean build from scratch succeeds: `./gradlew clean build`

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Integration tests flaky due to coroutine timing | Medium | Medium | Use `runTest` with virtual time; assert on event content, not timing |
| Screenshot capture requires manual app launch | High | Low | Accept manual step; document in README |
| Existing Maven build affected by Gradle files | Low | Medium | Verify `mvn compile` still works after all changes |

## Security Considerations

- None

## Next Steps

- Post-MVP: migrate remaining sorting algorithms (42 more)
- Post-MVP: migrate graph, DP, data structure categories
- Post-MVP: package as distributable `.dmg` / `.exe` / `.deb` via `compose.desktop.jPackage`
