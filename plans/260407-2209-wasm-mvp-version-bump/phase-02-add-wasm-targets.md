# Phase 2: Add wasmJs() Targets to Upstream Modules

## Context

algo-ui-web already has `wasmJs()` declared but its dependencies (algo-shared, algo-core, algo-viz-engine, algo-ui-shared) only have `jvm()` targets. Gradle can't resolve project dependencies without matching target platforms. Each upstream module must declare `wasmJs()` so Gradle publishes wasmJs variants.

## Overview

Add `wasmJs()` target to 4 modules. No source code changes — all code is already in `commonMain`.

## Requirements

- Each module keeps existing `jvm()` + `jvmTest` config untouched
- `wasmJs()` added after `jvm()` in kotlin block
- No new source sets needed (commonMain already contains all code)

## Implementation Steps

### Step 1: `algo-shared/build.gradle.kts`

**Current:**
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    jvmToolchain(20)
    jvm()
    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core)
        }
    }
}
```

**Replace `kotlin { ... }` block with:**
```kotlin
kotlin {
    jvmToolchain(20)
    jvm()
    wasmJs { browser() }
    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core)
        }
    }
}
```

### Step 2: `algo-core/build.gradle.kts`

**Replace `kotlin { ... }` block with:**
```kotlin
kotlin {
    jvmToolchain(20)
    jvm()
    wasmJs { browser() }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":algo-shared"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner)
            implementation(libs.kotest.assertions)
            implementation(libs.coroutines.core)
            implementation(libs.coroutines.test)
        }
    }
}
```

### Step 3: `algo-viz-engine/build.gradle.kts`

**Replace `kotlin { ... }` block with:**
```kotlin
kotlin {
    jvmToolchain(20)
    jvm()
    wasmJs { browser() }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":algo-shared"))
            implementation(project(":algo-core"))
            implementation(libs.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner)
            implementation(libs.kotest.assertions)
            implementation(libs.coroutines.test)
        }
    }
}
```

### Step 4: `algo-ui-shared/build.gradle.kts`

**Replace entire file with:**
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    jvmToolchain(20)
    jvm()
    wasmJs { browser() }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":algo-shared"))
            implementation(project(":algo-core"))
            implementation(project(":algo-viz-engine"))
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(libs.coroutines.core)
        }
    }
}
```

**Key change**: Also adds `alias(libs.plugins.compose.compiler)` — required for Kotlin 2.0+ Compose.

## Success Criteria

- `./gradlew :algo-shared:compileKotlinWasmJs` succeeds
- `./gradlew :algo-core:compileKotlinWasmJs` succeeds
- `./gradlew :algo-viz-engine:compileKotlinWasmJs` succeeds
- `./gradlew :algo-ui-shared:compileKotlinWasmJs` succeeds
- `./gradlew :algo-core:jvmTest` still passes (no regression)

## Risk Assessment

- **Low risk**: Pure additive change. `jvm()` target unchanged, `commonMain` source sets unchanged.
- **Fallback**: If wasmJs compile fails for a specific module, the error will point to any non-cross-platform API usage (shouldn't exist since code is already common).
- **`@OptIn(ExperimentalWasmDsl::class)` NOT needed**: wasmJs is stable in Kotlin 2.1.x. Remove this annotation where it exists.
