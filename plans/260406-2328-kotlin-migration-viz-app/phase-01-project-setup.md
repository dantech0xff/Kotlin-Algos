# Phase 01: Project Setup — Multi-Module Gradle + Compose Desktop Scaffold

**Date:** 2026-04-07
**Priority:** P1
**Status:** [ ] Pending

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: None (first phase)
- Research: [researcher-01-compose-viz-architecture.md](research/researcher-01-compose-viz-architecture.md), [researcher-02-kotlin-migration-patterns.md](research/researcher-02-kotlin-migration-patterns.md)

## Overview

Bootstrap the multi-module Gradle project with Compose Desktop plugin, version catalog, and all four modules. Replace Maven entirely — remove `pom.xml` and migrate all build configuration to Gradle Kotlin DSL.

## Key Insights

- **[VALIDATED]** Replace Maven entirely with Gradle. Remove `pom.xml` after migration.
- Current project uses Java 21 with JUnit 5, AssertJ, Mockito, Checkstyle, SpotBugs, PMD.
- Gradle Kotlin DSL + version catalog centralizes all dependency versions.
- Compose Multiplatform 1.6+ requires Kotlin 1.9.23+ and JVM toolchain 21.
- `compose.desktop.currentOs` handles platform-specific Swing/AWT dependencies.
- Existing Java source in `src/main/java/` stays in place — only build system changes.

## Requirements

### Functional
- Multi-module Gradle project compiles: algo-core, algo-viz-engine, algo-ui-desktop, algo-shared
- Compose Desktop app launches with a placeholder window
- Version catalog defines all shared dependency versions
- Each module has its own `build.gradle.kts`
- Existing Java source code still compiles via Gradle (not Maven)
- `pom.xml` removed after Gradle migration verified

### Non-Functional
- Build time < 30s for incremental compiles
- Single build system (Gradle only, no Maven)
- JVM toolchain: 21 (matches existing Java source level)

## Architecture

```
Kotlin-Algos/
├── build.gradle.kts              # Root: plugin management, subproject config
├── settings.gradle.kts           # Module includes
├── gradle/libs.versions.toml     # Version catalog
├── algo-core/                    # Pure Kotlin algorithms (no UI deps)
│   └── build.gradle.kts
├── algo-viz-engine/              # Visualization engine (coroutines, Flow)
│   └── build.gradle.kts
├── algo-ui-desktop/              # Compose Desktop app
│   └── build.gradle.kts
└── algo-shared/                  # Shared models, interfaces, events
    └── build.gradle.kts
```

**Dependency graph:**
```
algo-ui-desktop → algo-viz-engine → algo-core → algo-shared
                                       ↑              ↑
                                   algo-shared ────────┘
```

## Related Code Files

### Create
- `build.gradle.kts` (root)
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- `algo-core/build.gradle.kts`
- `algo-viz-engine/build.gradle.kts`
- `algo-ui-desktop/build.gradle.kts`
- `algo-shared/build.gradle.kts`
- `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/Main.kt` (placeholder window)
- `gradle/wrapper/gradle-wrapper.properties` (if not present)

### Modify
- Root project: add Gradle build files, remove Maven config

### Delete
- `pom.xml` — replaced by Gradle
- `.mvn/` directory — replaced by Gradle wrapper

## Implementation Steps

1. Create `gradle/libs.versions.toml` with versions for Kotlin, Compose, Coroutines, Kotest
2. Create `settings.gradle.kts` including all four modules + existing Java source as root module
3. Create root `build.gradle.kts` — migrate Java compilation, test, quality plugins from pom.xml
4. Create `algo-shared/build.gradle.kts` — pure Kotlin, no platform deps
5. Create `algo-core/build.gradle.kts` — depends on `:algo-shared`, coroutines
6. Create `algo-viz-engine/build.gradle.kts` — depends on `:algo-core`, `:algo-shared`, coroutines
7. Create `algo-ui-desktop/build.gradle.kts` — Compose Desktop plugin, depends on `:algo-viz-engine`
8. Create `algo-ui-desktop/src/jvmMain/kotlin/com/thealgorithms/ui/Main.kt` with Compose window scaffold
9. Add Gradle wrapper if missing (`gradle wrapper`)
10. Verify: `./gradlew build` compiles both existing Java + new Kotlin modules
11. Verify: `./gradlew :algo-ui-desktop:run` launches placeholder window
12. Remove `pom.xml` and `.mvn/` after verifying everything works

## Todo List

- [ ] Create `gradle/libs.versions.toml`
- [ ] Create `settings.gradle.kts`
- [ ] Create root `build.gradle.kts`
- [ ] Create `algo-shared/build.gradle.kts`
- [ ] Create `algo-core/build.gradle.kts`
- [ ] Create `algo-viz-engine/build.gradle.kts`
- [ ] Create `algo-ui-desktop/build.gradle.kts`
- [ ] Create `algo-ui-desktop/src/jvmMain/kotlin/.../Main.kt`
- [ ] Add Gradle wrapper
- [ ] Verify `./gradlew build` compiles all modules
- [ ] Verify `./gradlew :algo-ui-desktop:run` launches window

## Success Criteria

- [ ] `./gradlew build` passes with zero errors across all 4 modules
- [ ] `./gradlew :algo-ui-desktop:run` opens a Compose Desktop window
- [ ] Dependency graph confirmed via `./gradlew :algo-ui-desktop:dependencies`
- [ ] Existing Maven build (`mvn compile`) still works unaffected

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Compose plugin version incompatibility | Medium | High | Pin exact versions in catalog; test with `./gradlew` before proceeding |
| Gradle wrapper conflicts with existing Maven wrapper | Low | Low | Keep in separate `gradlew` vs `mvnw` scripts |
| JVM toolchain mismatch (21 vs 17) | Low | Medium | Explicitly set `jvmToolchain(21)` in all modules |

## Security Considerations

- No network calls in build scripts beyond Maven Central
- No secrets or credentials in build configuration

## Next Steps

- Phase 02 (Core Module) depends on this phase completing
- Phase 03 (Viz Engine) depends on Phase 02
- Phase 06 (Desktop UI) depends on Phase 03
