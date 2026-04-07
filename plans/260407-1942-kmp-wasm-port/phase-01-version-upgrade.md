# Phase 01: Version Upgrade — Kotlin 2.0+, Compose 1.7+, KMP Plugins

**Date:** 2026-04-07
**Priority:** P1
**Status:** [x] Complete

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: None (first phase)

## Overview

Upgrade Kotlin, Compose Multiplatform, and Gradle plugins to versions that support stable Wasm target. Add KMP plugin to version catalog.

## Key Insights

- Kotlin 2.0+ is required for stable Wasm target (1.9.x Wasm is experimental/beta)
- Compose Multiplatform 1.7.0+ provides full Wasm Canvas support
- Kotlin 2.0 introduced new K2 compiler frontend — may surface minor syntax warnings
- Gradle 8.5+ supports KMP without extra configuration

## Requirements

### Functional
- Kotlin upgraded to 2.0.21+ (or latest stable 2.x)
- Compose Multiplatform upgraded to 1.7.0+ (or latest compatible)
- kotlinx-coroutines upgraded to compatible version
- Kotest compatible with KMP (5.8+ supports multiplatform)
- `kotlin("multiplatform")` plugin added to version catalog
- All existing tests still pass after upgrade
- `./gradlew :algo-ui-desktop:run` still launches desktop app

### Non-Functional
- Zero breaking changes to existing code
- Build time should not increase significantly

## Architecture

### Version Changes

| Dependency | Current | Target | Notes |
|-----------|---------|--------|-------|
| Kotlin | 1.9.23 | 2.0.21 | K2 compiler, stable Wasm |
| Compose Multiplatform | 1.6.2 | 1.7.3 | Wasm Canvas support |
| Coroutines | 1.8.0 | 1.9.0 | Kotlin 2.0 compatible |
| Kotest | 5.8.1 | 5.9.1 | KMP support |
| Gradle | 8.5 (wrapper) | 8.5+ | No change needed |

### Build Config Changes

`gradle/libs.versions.toml`:
```toml
[versions]
kotlin = "2.0.21"
compose = "1.7.3"
coroutines = "1.9.0"
kotest = "5.9.1"

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
compose = { id = "org.jetbrains.compose", version.ref = "compose" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

Root `build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
}
```

## Related Code Files

### Modify
- `gradle/libs.versions.toml` — version bumps + new plugin entries
- `build.gradle.kts` (root) — add KMP + compose-compiler plugins
- `algo-shared/build.gradle.kts` — may need compose-compiler plugin
- `algo-core/build.gradle.kts` — may need compose-compiler plugin
- `algo-viz-engine/build.gradle.kts` — may need compose-compiler plugin
- `algo-ui-desktop/build.gradle.kts` — add compose-compiler plugin

### Create
- None

### Delete
- None

## Implementation Steps

1. Update `gradle/libs.versions.toml` with new versions + `kotlin-multiplatform` and `compose-compiler` plugin entries
2. Update root `build.gradle.kts` to declare new plugins with `apply false`
3. Add `compose-compiler` plugin to `algo-ui-desktop/build.gradle.kts` (Kotlin 2.0 requires explicit compose compiler plugin)
4. Run `./gradlew build` — fix any compilation errors from version upgrade
5. Run `./gradlew :algo-ui-desktop:run` — verify desktop app still works
6. Fix any deprecation warnings from Kotlin 2.0 migration

## Todo List

- [x] Update version catalog (`libs.versions.toml`)
- [x] Update root `build.gradle.kts` with new plugins
- [x] Add compose-compiler plugin to UI module
- [x] Fix compilation errors from version upgrade
- [x] Verify `./gradlew build` passes
- [x] Verify `./gradlew :algo-ui-desktop:run` works

## Success Criteria

- [x] All existing tests pass with new versions
- [x] Desktop app launches without errors
- [x] No deprecation warnings (or documented suppressions)
- [x] `./gradlew build` succeeds

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Kotlin 2.0 syntax changes break code | Low | Medium | Review compiler output, fix incrementally |
| Compose 1.7 API changes | Medium | Medium | Check Compose changelog, fix deprecated APIs |
| Kotest 5.9 incompatible with existing tests | Low | Low | Kotest has good backward compat |
| compose-compiler plugin required but missing | High | Low | Kotlin 2.0 changed how Compose compiler is integrated — must add explicitly |

## Security Considerations

- None (build dependency upgrades only)

## Next Steps

- Phase 02 (Restructure core modules) depends on this phase
