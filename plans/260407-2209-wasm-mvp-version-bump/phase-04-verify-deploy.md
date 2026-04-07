# Phase 4: Verify & Deploy

## Context

All code changes are done. Must verify no regressions, confirm wasm build produces working output, and update the CI/CD pipeline.

## Overview

1. Run full test suite
2. Verify desktop app works
3. Verify wasm build + local preview
4. Update deploy-wasm.yml for new toolchain

## Requirements

- All 130+ tests pass
- Desktop app launches and renders correctly
- Wasm build produces distributable
- GitHub Pages deployment works

## Implementation Steps

### Step 1: Run Full Test Suite

```bash
./gradlew :algo-core:jvmTest :algo-viz-engine:jvmTest
```

**Expected**: 130+ tests pass. Zero failures.

**If failures**: Likely Kotest/JUnit5 compatibility with Kotlin 2.1. Check test output. Tests are JVM-only so KMP issues don't apply.

### Step 2: Verify Desktop App

```bash
./gradlew :algo-ui-desktop:run
```

**Expected**: Window opens with full visualizer UI. Can select algorithms, run them, see visualization.

**If failure**: Check compose-compiler plugin is applied in algo-ui-desktop/build.gradle.kts.

### Step 3: Verify Wasm Build

```bash
./gradlew :algo-ui-web:wasmJsBrowserDistribution
```

**Expected**: BUILD SUCCESSFUL. Output at `algo-ui-web/build/dist/wasmJs/productionExecutable/`.

**Local preview** (optional):
```bash
./gradlew :algo-ui-web:wasmJsBrowserRun
```
Opens browser with live dev server.

### Step 4: Update `algo-ui-desktop/build.gradle.kts`

**Current desktop build file uses `alias(libs.plugins.compose)` but NOT compose.compiler.** With Kotlin 2.0+, the compose compiler plugin is mandatory for any module using Compose.

**Add compose-compiler plugin. Replace plugins block:**
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}
```

**This is critical** — without it, desktop build will fail at Kotlin 2.1.20.

### Step 5: Update `.github/workflows/deploy-wasm.yml`

**Current file** is mostly correct. Changes needed:

1. **Java version**: `20` → `21` (Kotlin 2.1.20 requires JDK 21 for toolchain)
2. **Setup Gradle**: Pin to v4 (already correct)
3. **Actions versions**: Update checkout/setup-java to v4 (checkout already v4, setup-java already v4)

**Specific change — line 35:**
```yaml
# Current
java-version: '20'

# Replace with
java-version: '21'
```

**Also update jvmToolchain in all build.gradle.kts files from 20 to 21:**

In `algo-shared`, `algo-core`, `algo-viz-engine`, `algo-ui-shared`, `algo-ui-web`:
```kotlin
// Current
jvmToolchain(20)

// Replace with
jvmToolchain(21)
```

**Rationale**: Kotlin 2.1.20 targets JDK 21 by default. Using JDK 20 toolchain may cause issues.

### Step 6: Verify Deploy Workflow

**Updated `deploy-wasm.yml` (full file):**
```yaml
name: Deploy Wasm to GitHub Pages

on:
  push:
    branches: [master]
    paths:
      - 'algo-shared/**'
      - 'algo-core/**'
      - 'algo-viz-engine/**'
      - 'algo-ui-shared/**'
      - 'algo-ui-web/**'
      - 'gradle/**'
      - 'build.gradle.kts'
      - 'settings.gradle.kts'
      - 'gradle.properties'
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: "pages"
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build Wasm distribution
        run: ./gradlew :algo-ui-web:wasmJsBrowserDistribution

      - name: Upload artifact
        uses: actions/upload-pages-artifact@v3
        with:
          path: algo-ui-web/build/dist/wasmJs/productionExecutable

  deploy:
    needs: build
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - name: Deploy to GitHub Pages
        id: deployment
        uses: actions/deploy-pages@v4
```

**Only change from current: `java-version: '20'` → `'21'`**

## Success Criteria

- [ ] `./gradlew :algo-core:jvmTest :algo-viz-engine:jvmTest` — all 130+ tests pass
- [ ] `./gradlew :algo-ui-desktop:run` — desktop app works
- [ ] `./gradlew :algo-ui-web:wasmJsBrowserDistribution` — build succeeds
- [ ] `algo-ui-web/build/dist/wasmJs/productionExecutable/index.html` exists and references `algo-viz.js`
- [ ] `deploy-wasm.yml` uses Java 21
- [ ] All `jvmToolchain(20)` → `jvmToolchain(21)` across all modules

## Risk Assessment

- **Low risk**: Verification-only phase. If Phases 1-3 succeeded, this should pass.
- **JDK 21 requirement**: CI must use JDK 21. Kotlin 2.1.20 + Compose 1.8.2 recommends JDK 21 toolchain.
- **GitHub Pages**: One-time setup of Pages source to "GitHub Actions" in repo settings. If not done, deploy step fails with clear instructions.

## Files Modified in This Phase

| File | Change |
|------|--------|
| `algo-ui-desktop/build.gradle.kts` | Add `alias(libs.plugins.compose.compiler)` |
| `algo-shared/build.gradle.kts` | `jvmToolchain(20)` → `jvmToolchain(21)` |
| `algo-core/build.gradle.kts` | `jvmToolchain(20)` → `jvmToolchain(21)` |
| `algo-viz-engine/build.gradle.kts` | `jvmToolchain(20)` → `jvmToolchain(21)` |
| `algo-ui-shared/build.gradle.kts` | `jvmToolchain(20)` → `jvmToolchain(21)` |
| `algo-ui-web/build.gradle.kts` | `jvmToolchain(20)` → `jvmToolchain(21)` |
| `.github/workflows/deploy-wasm.yml` | `java-version: '20'` → `'21'` |
