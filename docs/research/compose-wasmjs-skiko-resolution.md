# Compose Multiplatform wasmJs + Skiko Resolution Plan

**Date:** 2026-04-07
**Status:** Research Complete
**Problem:** `Module not found: Error: Can't resolve './skiko.mjs'` when building wasmJs target with Kotlin 1.9.23 + Compose 1.6.2

---

## 1. Is this a known issue?

**Yes. Confirmed known issue.**

- **JetBrains/compose-multiplatform#4133** — Exact same error. User reports `Can't resolve './skiko.mjs'` when using Compose animation dependencies on wasmJs target. Even with `compose.web {}` DSL disabled, the wasm build still expects `skiko.mjs`.
- Root cause: Compose Multiplatform 1.6.x wasmJs support was **Alpha** quality. The Skiko wasm artifact wasn't consistently packaged or resolved by webpack, especially with Kotlin 1.9.x (which lacked stable wasm K2 backend).

---

## 2. Minimum Kotlin + Compose for Stable wasmJs (Canvas-based Compose UI)

**Minimum: Compose 1.7.0 + Kotlin 2.0.21**

| Component | Minimum Stable | Recommended (Current) |
|-----------|---------------|----------------------|
| Compose Multiplatform | **1.7.0** (Oct 2024) | **1.10.3** |
| Kotlin | **2.0.21** | **2.1.20** |
| Gradle | **8.5** | **8.11+** |
| JVM Toolchain | 17 | 17-21 |
| Coroutines | 1.8.0 | 1.9.0+ |

**Why 1.7.0 minimum:**
- Compose 1.7.0 was the first stable release with production-ready wasmJs web support
- Fixed: `skiko.js` declared redundant for K/Wasm apps (PR #5134)
- Fixed: duplicate `skiko.wasm` in web distribution (PR #4958)
- Fixed: multiple web rendering crashes
- **Compose 1.8.0+** fully transitioned to K2 compiler, requires Kotlin 2.1.0+

**Version timeline:**
- 1.6.2 (Apr 2024) — wasmJs Alpha, Kotlin 2.0-RC1 support added
- 1.6.11 — last 1.6.x, wasmJs still Alpha
- **1.7.0** (Oct 2024) — wasmJs declared **stable**
- 1.7.3 — stable maintenance, recommended for production if not upgrading further
- 1.8.0+ — requires Kotlin 2.1+, K2 only
- 1.10.3 — current latest stable

---

## 3. Correct build.gradle.kts Configuration

### Updated `gradle/libs.versions.toml`

```toml
[versions]
kotlin = "2.1.20"
compose = "1.8.2"
coroutines = "1.9.0"

[libraries]
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
compose = { id = "org.jetbrains.compose", version.ref = "compose" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

**Critical:** Starting with Kotlin 2.0+, you need the **Compose Compiler Gradle plugin** (`org.jetbrains.kotlin.plugin.compose`) applied alongside the Compose Multiplatform plugin. This replaces the old compiler extension approach.

### Updated `algo-ui-web/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)  // NEW: Required since Kotlin 2.0
}

group = "com.thealgorithms"
version = "1.0.0"

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "algo-viz.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(libs.coroutines.core)
        }
    }
}
```

**Key changes from current config:**
1. **Remove** `@OptIn(ExperimentalWasmDsl::class)` — `wasmJs {}` is stable since Kotlin 2.0
2. **Add** `compose-compiler` plugin
3. **Remove** `jvmToolchain(20)` — not needed for pure wasmJs module; or change to `jvmToolchain(17)`
4. **No explicit Skiko dependency needed** — the `org.jetbrains.compose` plugin resolves it transitively

### Root `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false  // NEW
}
```

### No custom webpack config needed

With Compose 1.7.0+, Skiko artifacts are correctly resolved automatically. No `webpack.config.js` overrides, no `NormalModuleReplacementPlugin`, no explicit skiko dependency.

---

## 4. Skiko Wasm Rendering Backend Stability

**Stable since Compose 1.7.0 (Oct 2024)**

| Version | Skiko Wasm Status |
|---------|------------------|
| < 1.6.0 | Experimental/Alpha |
| 1.6.0–1.6.11 | Alpha — works but frequent breakage |
| **1.7.0** | **Stable** — Skia m126, production-ready |
| 1.8.0+ | Stable — continues improving |
| 1.10.3 | Stable — Skia m140+ |

Skiko provides the Skia rendering backend for Compose on non-Android platforms. The wasm variant compiles Skia to WebAssembly. The `skiko.wasm` artifact (~9MB) contains the Skia renderer. In Compose 1.7.0+, `skiko.js` was made redundant for K/Wasm (only `skiko.wasm` is needed), reducing bundle confusion.

---

## 5. Workarounds (If You Can't Upgrade)

### Option A: Custom webpack config (fragile, not recommended)

```kotlin
// In algo-ui-web/build.gradle.kts — DOES NOT reliably work
wasmJs {
    browser {
        commonWebpackConfig {
            outputFileName = "algo-viz.js"
        }
    }
    binaries.executable()
}
```

From issue #4133, user tried `NormalModuleReplacementPlugin` to stub `skiko.mjs` — dev builds crash at runtime with `import object field 'org_jetbrains_skia_Bitmap__1nMake' is not a Function`. Production builds with binaryen optimization sometimes worked. **Not viable for Canvas-based UI.**

### Option B: Upgrade versions (RECOMMENDED)

The only reliable fix is upgrading:
1. Kotlin → 2.0.21 minimum (2.1.20 recommended)
2. Compose → 1.7.0 minimum (1.8.2+ recommended)
3. Add Compose Compiler plugin

---

## Action Plan for This Project

### Phase 1: Version Upgrade

1. Update `gradle/libs.versions.toml`:
   - `kotlin = "2.1.20"`
   - `compose = "1.8.2"`
   - `coroutines = "1.9.0"`
   - Add `compose-compiler` plugin entry

2. Update root `build.gradle.kts` — add `compose-compiler` plugin

3. Update `algo-ui-web/build.gradle.kts`:
   - Add `compose-compiler` plugin
   - Remove `@OptIn(ExperimentalWasmDsl::class)`
   - Remove `jvmToolchain(20)` (or keep if needed by JVM targets)

4. Update all other module `build.gradle.kts` files to add `compose-compiler` plugin

5. Update Gradle wrapper to 8.11+ if needed

### Phase 2: Cross-module wasmJs Support (Future)

To fully enable wasmJs with shared modules:
1. Add `wasmJs()` target to `algo-shared`, `algo-core`, `algo-viz-engine`, `algo-ui-shared`
2. Move source sets from `jvmMain` to `commonMain` where possible
3. Uncomment `project()` dependencies in `algo-ui-web`
4. Replace placeholder `Main.kt` with full `AppContent` integration

### Risk Assessment

- **Low risk:** Version upgrade itself (Kotlin 2.x + Compose 1.8.x well-tested)
- **Medium risk:** `jvmToolchain(20)` — Kotlin 2.1.x officially supports JDK 17-21. JDK 20 is non-LTS. Consider downgrading to 17.
- **High risk:** None identified. The upgrade path is well-documented.

---

## Unresolved Questions

1. **Gradle version:** Current project specifies Gradle 8.5. Kotlin 2.1.20 requires Gradle 8.10+. Need to verify current wrapper version.
2. **JVM toolchain:** JDK 20 is non-LTS and may cause issues. Should migrate to JDK 17 (LTS) or 21 (LTS).
3. **Coroutines wasmJs:** `kotlinx-coroutines-core:1.8.0` does support wasmJs, but 1.9.0+ has better wasm support. Verify no breaking API changes.
4. **compose-compiler plugin + JVM-only modules:** Modules like `algo-ui-desktop` use `org.jetbrains.kotlin.jvm` — the compose-compiler plugin must also be applied there. Verify compatibility.
