# Compose Multiplatform Algorithm Visualization Architecture Research

**Date:** 2026-04-07
**Focus:** Architecture patterns for Kotlin algorithm visualization desktop app

## 1. Compose Multiplatform Desktop Setup (2025-2026)

### Recommended Project Structure
```kotlin
// Root build.gradle.kts
plugins {
    kotlin("multiplatform") version "1.9.23"
    kotlin("plugin.compose") version "1.9.23"
    id("org.jetbrains.compose") version "1.5.13"
}

kotlin {
    jvm("desktop") {
        jvmToolchain(21)
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xskip-prerelease-check")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.uiTooling)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
            }
        }
    }
}
```

### Multi-Module Structure
```
Kotlin-Algos/
├── algo-core/                    # Pure Kotlin algorithms
│   └── src/commonMain/kotlin/com/thealgorithms/
├── algo-viz-engine/            # Visualization engine
│   └── src/commonMain/kotlin/com/thealgorithms/viz/
├── algo-ui-desktop/             # Compose Desktop app
│   └── src/desktopMain/kotlin/com/thealgorithms/ui/
├── algo-shared/                # Shared models & interfaces
│   └── src/commonMain/kotlin/com/thealgorithms/common/
└── gradle/wrapper/
```

## 2. Algorithm Visualization Architecture

### Event-Driven Pattern with Kotlin Flow
```kotlin
// Shared model
sealed interface AlgorithmEvent {
    data class Compare(val left: Int, val right: Int) : AlgorithmEvent
    data class Swap(val from: Int, val to: Int) : AlgorithmEvent
    data class Select(val index: Int) : AlgorithmEvent
    data class Pivot(val index: Int) : AlgorithmEvent
    data class Complete(val result: List<Any>) : AlgorithmEvent
}

// Algorithm interface
interface VisualizableAlgorithm {
    suspend fun execute(input: List<Int>, eventEmitter: SharedFlow<AlgorithmEvent>)
}

// Implementation example
class BubbleSortAlgorithm : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, eventEmitter: SharedFlow<AlgorithmEvent>) {
        val array = input.toIntArray()
        eventEmitter.emit(AlgorithmEvent.Start(input))

        for (i in 1 until array.size) {
            eventEmitter.emit(AlgorithmEvent.Select(i))

            for (j in 0 until array.size - i) {
                eventEmitter.emit(AlgorithmEvent.Compare(j, j + 1))

                if (array[j] > array[j + 1]) {
                    eventEmitter.emit(AlgorithmEvent.Swap(j, j + 1))
                    array.swap(j, j + 1)
                }
            }
        }

        eventEmitter.emit(AlgorithmEvent.Complete(array.toList()))
    }
}
```

### Open Source Patterns Analysis
From VisuAlgo and AlgorithmVisualizer:
- **Step-based execution**: Algorithms yield control after each significant operation
- **Event emission**: Decouples algorithm logic from visualization
- **State management**: Single source of truth for algorithm state
- **Playback control**: Buffer events for step-back functionality

## 3. Animation in Compose Desktop

### Canvas-Based Rendering
```kotlin
@Composable
fun AnimatedBarChart(
    data: List<Int>,
    highlights: Set<Int> = emptySet(),
    animationProgress: Float = 1f,
    modifier: Modifier = Modifier
) {
    val canvasWidth = 400.dp
    val canvasHeight = 300.dp
    val max = data.maxOrNull() ?: 1

    Canvas(
        modifier = modifier.size(canvasWidth, canvasHeight),
        onDraw = {
            val barWidth = (size.width - (data.size - 1) * spacing) / data.size
            val spacing = 4.dp.toPx()

            data.forEachIndexed { index, value ->
                val barHeight = (value.toFloat() / max) * size.height * animationProgress
                val x = index * (barWidth + spacing)
                val y = size.height - barHeight

                // Animated bar drawing
                drawRect(
                    color = if (highlights.contains(index)) Color.Red else Color.Blue,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    style = fillStyle
                )

                // Value label
                drawText(
                    text = value.toString(),
                    topLeft = Offset(x + barWidth/2 - 10, y - 25)
                )
            }
        }
    )
}
```

### Tree/Graph Rendering
```kotlin
@Composable
fun TreeNode(
    value: Int,
    isHighlighted: Boolean = false,
    onClick: () -> Unit = {}
) {
    val bgColor = if (isHighlighted) Color.Red else Color.Blue
    val textColor = Color.White

    Card(
        modifier = Modifier.size(60.dp, 60.dp),
        backgroundColor = bgColor,
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillSize()
        ) {
            Text(
                text = value.toString(),
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

## 4. Streaming vs Snapshot Playback

### Streaming Pattern (Chosen)
**Pros:**
- Real-time visualization
- Lower memory usage
- Responsive controls
- Natural algorithm flow

**Cons:**
- Step-back requires event buffering
- Complex state management
- Timing sensitive

**Implementation:**
```kotlin
class AlgorithmPlayer {
    private val events = mutableListOf<AlgorithmEvent>()
    private var currentIndex = 0
    private val playbackState = MutableStateFlow(PlaybackState.Stopped)

    suspend fun playAlgorithm(algorithm: VisualizableAlgorithm, input: List<Int>) {
        events.clear()
        currentIndex = 0

        coroutineScope {
            val eventFlow = MutableSharedFlow<AlgorithmEvent>()

            launch {
                algorithm.execute(input, eventFlow)
            }

            eventFlow.collect { event ->
                events.add(event)
                playbackState.value = PlaybackState.Playing
            }
        }
    }

    fun stepForward() {
        if (currentIndex < events.size - 1) {
            currentIndex++
        }
    }

    fun stepBack() {
        if (currentIndex > 0) {
            currentIndex--
        }
    }
}
```

### Hybrid Approach (Recommended)
Combine streaming for real-time playback with periodic snapshots for step-back capability:
```kotlin
data class AlgorithmSnapshot(
    val events: List<AlgorithmEvent>,
    val currentState: List<Any>,
    val timestamp: Long
)

class HybridAlgorithmPlayer {
    private val snapshots = mutableListOf<AlgorithmSnapshot>()
    private val streamingBuffer = mutableListOf<AlgorithmEvent>()
    private var currentState: List<Any> = emptyList()

    fun onEvent(event: AlgorithmEvent) {
        streamingBuffer.add(event)
        updateState(event)

        // Create snapshot every 10 events
        if (streamingBuffer.size % 10 == 0) {
            snapshots.add(
                AlgorithmSnapshot(
                    events = streamingBuffer.toList(),
                    currentState = currentState,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
```

## Key Technical Recommendations

1. **Use Flow/SharedFlow** for real-time event streaming
2. **Canvas API** for custom animations and visualizations
3. **Hybrid approach** combining streaming playback with snapshot-based step-back
4. **Multi-module Gradle** for clean separation of concerns
5. **StateFlow** for reactive UI updates
6. **LaunchedEffect** for animation timing control

## Unresolved Questions

- Optimal event granularity (too many events = performance issues, too few = poor visualization)
- Best way to handle complex state updates for nested data structures
- Performance optimization for large datasets (>1000 elements)