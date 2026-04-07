# Phase 1: Version Bump

## Context

Kotlin 1.9.23 + Compose 1.6.2 = wasmJs was Alpha. Skiko wasm artifacts not reliably resolved (JetBrains/compose-multiplatform#4133). Kotlin 2.0+ restructured the Compose compiler as a Gradle plugin. Must bump everything together.

## Overview

Update version catalog, add compose-compiler plugin to root build.

## Requirements

- Kotlin 2.1.20
- Compose Multiplatform 1.8.2
- Compose Compiler Plugin (new in Kotlin 2.0+, replaces old compiler integration)
- Coroutines 1.10.1
- Kotest stays at 5.8.1 (JVM-only tests, no KMP needed)

## Implementation Steps

### Step 1: Update `gradle/libs.versions.toml`

**Current:**
```toml
[versions]
kotlin = "1.9.23"
compose = "1.6.2"
coroutines = "1.8.0"
kotest = "5.8.1"

[libraries]
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
kotest-runner = { module = "io.kotest:kotest-runner-junit5", version.ref = "kotest" }
kotest-assertions = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
compose = { id = "org.jetbrains.compose", version.ref = "compose" }
```

**Replace with:**
```toml
[versions]
kotlin = "2.1.20"
compose = "1.8.2"
coroutines = "1.10.1"
kotest = "5.8.1"

[libraries]
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
kotest-runner = { module = "io.kotest:kotest-runner-junit5", version.ref = "kotest" }
kotest-assertions = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
compose = { id = "org.jetbrains.compose", version.ref = "compose" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

### Step 2: Update `build.gradle.kts` (root)

**Current:**
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose) apply false
}
```

**Replace with:**
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
}
```

## Success Criteria

- `./gradlew help` succeeds without errors (validates version catalog resolution)
- No compilation attempted yet — just version catalog + plugin registration

## Risk Assessment

- **Low risk**: Pure configuration change. If version catalog is wrong, Gradle sync fails immediately with clear error.
- **Rollback**: Revert 2 files.
