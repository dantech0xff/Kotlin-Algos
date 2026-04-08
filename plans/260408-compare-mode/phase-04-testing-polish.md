# Phase 04: Testing + Polish

## Context
- Parent: [plan.md](./plan.md)
- Depends on: [phase-03-integration.md](./phase-03-integration.md)

## Overview
- **Date**: 2026-04-08
- **Priority**: P1
- **Status**: Pending
- **Effort**: 1h

Run tests, visual QA on desktop, fix any issues, verify web build.

## Requirements
- All existing 197 tests pass
- Both Desktop + Web compile
- Visual QA: run desktop app, test compare mode end-to-end
- Fix any rendering issues in 2×2 grid

## Implementation Steps

1. Run all existing tests: `./gradlew :algo-core:jvmTest :algo-viz-engine:jvmTest`
2. Build all platforms: `./gradlew :algo-ui-shared:compileKotlinWasmJs :algo-ui-desktop:compileKotlin`
3. Run desktop app: `./gradlew :algo-ui-desktop:run`
4. Visual QA checklist:
   - [ ] Sidebar shows "Compare" button
   - [ ] Clicking "Compare" opens picker dialog
   - [ ] Picker allows exactly 4 selections
   - [ ] Confirming shows 2×2 grid with 4 algorithms
   - [ ] Play button advances all 4 synchronized
   - [ ] Each slot shows algorithm name, bars, stats
   - [ ] Slots with fewer events show "Complete" badge
   - [ ] Speed slider works
   - [ ] Input presets work (same input to all 4)
   - [ ] "Back to Single" returns to normal mode
   - [ ] Single mode completely unaffected
5. Fix any issues found
6. Final build + test run

## Todo
- [ ] Run all existing tests
- [ ] Build all platforms
- [ ] Desktop visual QA
- [ ] Fix any issues
- [ ] Final build verification

## Success Criteria
- [ ] All 197 existing tests pass
- [ ] Desktop + Web both compile
- [ ] Compare mode works end-to-end on desktop
- [ ] Single-algorithm view unaffected
- [ ] No regressions

## Next Steps
- Commit and deploy
