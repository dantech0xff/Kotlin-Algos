# The Algorithms - Java

[![Build](https://github.com/TheAlgorithms/Java/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/TheAlgorithms/Java/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/TheAlgorithms/Java/graph/badge.svg?token=XAdPyqTIqR)](https://codecov.io/gh/TheAlgorithms/Java)
[![Discord chat](https://img.shields.io/discord/808045925556682782.svg?logo=discord&colorB=7289DA&style=flat-square)](https://discord.gg/c7MnfGFGa6)
[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/TheAlgorithms/Java)


You can run and edit the algorithms, or contribute to them using Gitpod.io (a free online development environment) with a single click.

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/TheAlgorithms/Java)

### All algorithms are implemented in Java (for educational purposes)
These implementations are intended for learning purposes. As such, they may be less efficient than the Java standard library.

## Contribution Guidelines
Please read our [Contribution Guidelines](CONTRIBUTING.md) before you contribute to this project.

## Kotlin Algorithm Visualizer

A Compose Desktop application that visualizes sorting and search algorithms step-by-step.

### Quick Start
```bash
./gradlew :algo-ui-desktop:run
```

### Module Structure
| Module | Purpose |
|--------|---------|
| `algo-shared` | Interfaces, events, playback state |
| `algo-core` | Algorithm implementations + extensions |
| `algo-viz-engine` | Visualization engine (player, snapshots, playback) |
| `algo-ui-desktop` | Compose Desktop app |

### MVP Algorithms
**Sorting:** BubbleSort, SelectionSort, InsertionSort, QuickSort, MergeSort
**Search:** LinearSearch, IterativeBinarySearch, RecursiveBinarySearch

### Features
- Step-by-step visualization with playback controls
- Speed control (10ms - 2000ms per step)
- Keyboard shortcuts (Space=play/pause, Left/Right=step, R=reset)
- Stats panel showing comparisons and swaps
- Pseudocode panel with active-line highlighting synced to each step
- Step-by-step explanations describing what's happening and why
- Complexity info (best/avg/worst time, space, stability, difficulty)
- Input presets (Random, Nearly Sorted, Reversed, All Equal, Many Duplicates)
- Progress scrubber to seek to any event
- Color legend explaining visual highlight meanings
- Sidebar search filter to quickly find algorithms

## Algorithms
Our [directory](DIRECTORY.md) has the full list of applications.
