# Phase 06: GitHub Pages Deployment — CI/CD Pipeline

**Date:** 2026-04-07
**Priority:** P2
**Status:** [x] Complete

## Context

- Parent plan: [plan.md](plan.md)
- Dependencies: Phase 04 (Wasm module builds successfully)

## Overview

Set up GitHub Actions workflow to auto-build the Wasm app and deploy to GitHub Pages on every push to `master`.

## Key Insights

- `./gradlew :algo-ui-web:wasmJsBrowserDistribution` produces static files in `build/dist/wasmJs/productionExecutable/`
- GitHub Pages can serve from `gh-pages` branch or `docs/` folder
- Actions workflow triggers on push to `master`
- Wasm needs correct MIME types — `application/wasm` for `.wasm` files
- Base href may need configuration if deploying to `<username>.github.io/<repo>/`

## Requirements

### Functional
- GitHub Actions workflow: build Wasm → deploy to GitHub Pages
- Triggers on push to `master` (and manual dispatch)
- Deploys only if build + tests pass
- Correct MIME types for `.wasm` files
- Custom 404 page for SPA routing (if needed)

### Non-Functional
- Build + deploy < 5 minutes total
- Only rebuilds on relevant changes (path filters)

## Architecture

### Workflow File
`.github/workflows/deploy-wasm.yml`:

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
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: "pages"
  cancel-in-progress: true

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '20'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build Wasm distribution
        run: ./gradlew :algo-ui-web:wasmJsBrowserDistribution

      - name: Fix MIME types
        run: |
          cat > build/dist/wasmJs/productionExecutable/.htaccess << 'EOF'
          AddType application/wasm .wasm
          AddType application/javascript .js
          AddType text/html .html
          EOF

      - name: Upload artifact
        uses: actions/upload-pages-artifact@v3
        with:
          path: build/dist/wasmJs/productionExecutable

      - name: Deploy to GitHub Pages
        id: deployment
        uses: actions/deploy-pages@v4
```

### Alternative: Static Deployment (simpler)
If GitHub Pages from `gh-pages` branch is preferred:

```yaml
      - name: Deploy to gh-pages branch
        run: |
          git config user.name "github-actions[bot]"
          git config user.email "github-actions[bot]@users.noreply.github.com"
          git checkout --orphan gh-pages
          git rm -rf .
          cp -r build/dist/wasmJs/productionExecutable/* .
          echo "AddType application/wasm .wasm" > .htaccess
          git add .
          git commit -m "deploy: wasm visualization app"
          git push origin gh-pages --force
```

## Related Code Files

### Create
- `.github/workflows/deploy-wasm.yml`

### Modify
- None

### Read (reference)
- `.github/workflows/build.yml` — existing CI workflow patterns

## Implementation Steps

1. Read `.github/workflows/build.yml` to match existing CI patterns
2. Create `.github/workflows/deploy-wasm.yml` with Actions Pages deployment
3. Verify: push to a test branch, check Actions tab
4. Verify: deployed site loads at `https://<org>.github.io/<repo>/`
5. Add base href to `index.html` if deploying to subpath: `<base href="/<repo>/">`

## Todo List
- [x] Read existing CI workflow patterns
- [x] Create `deploy-wasm.yml` workflow
- [x] Test workflow triggers correctly
- [x] Verify deployed site loads
- [x] Test all 8 algorithms in browser

## Success Criteria
- [x] Push to `master` triggers Actions build
- [x] Wasm app deployed to GitHub Pages automatically
- [x] App loads and all algorithms work in browser
- [x] Build + deploy completes in < 5 minutes

## Risk Assessment
| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| GitHub Pages not enabled on repo | Medium | High | Document setup step in README |
| Base href mismatch (subpath deployment) | Medium | Medium | Add `<base href>` to index.html |
| Wasm MIME type not served correctly | Low | Medium | `.htaccess` file handles this |
| Build timeout on Actions (first run) | Medium | Low | Gradle caching via `gradle/actions/setup-gradle@v4` |

## Security Considerations
- `permissions` scoped to `pages: write` only — no repo write access
- No secrets or tokens needed (OIDC auth via `id-token: write`)
- `concurrency` group prevents parallel deployments

## Next Steps
- **All phases complete** — KMP Wasm port complete! 🚀
- Project now serves both web and desktop from shared codebase
