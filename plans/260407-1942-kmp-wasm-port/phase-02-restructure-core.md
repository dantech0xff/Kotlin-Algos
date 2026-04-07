# Phase 02: Restructure Core Modules — Move to commonMain

**Date:** 2026-04-07
**Priority:** P1
**Status:** [x] Complete

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: Phase 01 (version upgrade)

## Overview

Convert `algo-shared`, `algo-core`, and `algo-viz-engine` from JVM-only (`kotlin("jvm")`) to Kotlin Multiplatform (`kotlin("multiplatform")`). Move source from `src/main/kotlin` → `src/commonMain/kotlin` and tests from `src/test/kotlin` → `src/commonTest/kotlin`.

## Key Insights

- These 3 modules contain ZERO JVM-specific code (no `java.*` imports) — pure Kotlin + coroutines
- `kotlin("multiplatform")` plugin replaces `kotlin("jvm")` and `java-library`
- Source set structure: `src/commonMain/kotlin/` for shared code, `src/commonTest/kotlin/` for shared tests
- No `jvmMain`/`wasmJsMain` needed initially — all code goes to `commonMain`
- `jvmToolchain(20)` moves from per-module to root config or stays in JVM targets only

## Requirements

### Functional
- All 3 modules build with `kotlin("multiplatform")` plugin
- Source files moved to `commonMain`/`commonTest` source sets
- All existing tests still pass
- `algo-core` and `algo-viz-engine` resolve dependencies on `algo-shared` via multiplatform dependency

### Non-Functional
- Zero functional changes to algorithm or engine code
- Build time should not increase

## Architecture

### Module: algo-shared

**Before:**
```
algo-shared/src/main/kotlin/com/thealgorithms/shared/*.kt
algo-shared/build.gradle.kts  (kotlin("jvm"))
```

**After:**
```
algo-shared/src/commonMain/kotlin/com/thealgorithms/shared/*.kt
algo-shared/build.gradle.kts  (kotlin("multiplatform"))
```

Build file:
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    jvm()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core)
        }
    }
}
```

### Module: algo-core

**Before:**
```
algo-core/src/main/kotlin/com/thealgorithms/core/sorts/*.kt
algo-core/src/main/kotlin/com/thealgorithms/core/searches/*.kt
algo-core/src/main/kotlin/com/thealgorithms/core/utils/*.kt
algo-core/src/test/kotlin/com/thealgorithms/core/sorts/*Test.kt
algo-core/src/test/kotlin/com/thealgorithms/core/searches/*Test.kt
algo-core/src/test/kotlin/com/thealgorithms/core/SortUtilsTest.kt
algo-core/build.gradle.kts  (kotlin("jvm"))
```

**After:**
```
algo-core/src/commonMain/kotlin/...  (all source)
algo-core/src/commonTest/kotlin/...  (all tests)
algo-core/build.gradle.kts  (kotlin("multiplatform"))
```

Build file:
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    jvm()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":algo-shared"))
        }
        commonTest.dependencies {
            implementation(libs.kotest.runner)
            implementation(libs.kotest.assertions)
            implementation(libs.coroutines.core)
            implementation(libs.coroutines.test)
        }
    }
}
```

**NOTE:** Kotest JUnit5 runner may not work on Wasm. Solution:
- Keep tests in `commonTest` with `@OptIn` annotations
- Or split: `commonTest` for shared logic, `jvmTest` for Kotest-specific
- **Recommended:** Use `kotlin("test")` for common tests, keep Kotest in JVM-only test source set

### Module: algo-viz-engine

Same pattern as algo-core.

Build file:
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    jvm()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":algo-shared"))
            implementation(project(":algo-core"))
            implementation(libs.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotest.runner)
            implementation(libs.kotest.assertions)
            implementation(libs.coroutines.test)
        }
    }
}
```

## Related Code Files

### Move (algo-shared)
- `algo-shared/src/main/kotlin/` → `algo-shared/src/commonMain/kotlin/`
  - All 7 files (AlgorithmEvent, PlaybackState, SortAlgorithm, SearchAlgorithm, VisualizableAlgorithm, SearchVisualizableAlgorithm, SearchInput)

### Move (algo-core)
- `algo-core/src/main/kotlin/` → `algo-core/src/commonMain/kotlin/`
  - sorts/BubbleSort.kt, SelectionSort.kt, InsertionSort.kt, QuickSort.kt, MergeSort.kt
  - searches/LinearSearch.kt, IterativeBinarySearch.kt, RecursiveBinarySearch.kt
  - utils/SortUtils.kt
- `algo-core/src/test/kotlin/` → `algo-core/src/commonTest/kotlin/`
  - All test files

### Move (algo-viz-engine)
- `algo-viz-engine/src/main/kotlin/` → `algo-viz-engine/src/commonMain/kotlin/`
  - AlgorithmPlayer.kt, AlgorithmSnapshot.kt, EventBuffer.kt, SnapshotReconstructor.kt, MockVisualizableAlgorithm.kt, RecordingEmitter (if internal)
- `algo-viz-engine/src/test/kotlin/` → `algo-viz-engine/src/commonTest/kotlin/`
  - All test + integration files

### Modify
- `algo-shared/build.gradle.kts`
- `algo-core/build.gradle.kts`
- `algo-viz-engine/build.gradle.kts`
- `settings.gradle.kts` — add `algo-ui-shared`, `algo-ui-web`

### Create
- Directories: `src/commonMain/kotlin/`, `src/commonTest/kotlin/` for each module

### Delete
- Old `src/main/kotlin/`, `src/test/kotlin/` directories after move

## Implementation Steps

1. Create `commonMain`/`commonTest` directory structures for all 3 modules
2. Move algo-shared source files from `src/main/` → `src/commonMain/`
3. Update `algo-shared/build.gradle.kts` to use `kotlin("multiplatform")` with `jvm()` + `wasmJs()` targets
4. Verify: `./gradlew :algo-shared:build` passes
5. Move algo-core source + test files
6. Update `algo-core/build.gradle.kts` for multiplatform
7. Verify: `./gradlew :algo-core:build` passes
8. Move algo-viz-engine source + test files
9. Update `algo-viz-engine/build.gradle.kts` for multiplatform
10. Verify: `./gradlew :algo-viz-engine:build` passes
11. Delete old `src/main/` and `src/test/` directories
12. Full verification: `./gradlew check` passes

## Todo List

- [x] Restructure algo-shared (source move + build file)
- [x] Restructure algo-core (source move + build file)
- [x] Restructure algo-viz-engine (source move + build file)
- [x] Update settings.gradle.kts with new modules
- [x] Clean up old source directories
- [x] Verify `./gradlew check` passes

## Success Criteria

- [x] All 3 modules build with `kotlin("multiplatform")` plugin
- [x] Source in `commonMain`/`commonTest` source sets
- [x] All existing tests pass
- [x] No JVM-specific imports in commonMain code

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Kotest not fully KMP-compatible | Medium | Medium | Keep Kotest in JVM test source set; use kotlin("test") for commonTest |
| MutableSharedFlow API differences on Wasm | Low | Low | kotlinx-coroutines-core is fully KMP |
| File moves break git history | Low | Low | Use `git mv` to preserve history |

## Security Considerations

- None (source reorganization only)

## Next Steps

- Phase 03 (Extract shared UI) depends on this phase
