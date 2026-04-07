package com.thealgorithms.viz.integration

import com.thealgorithms.core.sorts.BubbleSortVisualizer
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.PlaybackState
import com.thealgorithms.viz.AlgorithmPlayer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSorted
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class BubbleSortIntegrationTest : FunSpec({

    test("player runs BubbleSort and produces events with sorted final state") {
        runTest {
            val player = AlgorithmPlayer()
            val input = listOf(3, 1, 2)

            player.run(BubbleSortVisualizer(), input)

            // Should have at least Start + some operations + Complete
            player.totalEvents.value shouldBe 7 // Start + 3 Compare + 2 Swap + Complete

            val snapshot = player.currentSnapshot.value
            snapshot.arrayState shouldBe listOf(3, 1, 2) // initial state at index 0

            player.destroy()
        }
    }

    test("stepping forward through all events yields sorted output") {
        runTest {
            val player = AlgorithmPlayer()
            player.run(BubbleSortVisualizer(), listOf(2, 1))

            player.currentEventIndex.value shouldBe 0

            // Step to the last event
            while (player.currentEventIndex.value < player.totalEvents.value - 1) {
                player.stepForward()
            }

            player.currentSnapshot.value.arrayState shouldBe listOf(1, 2)
            player.state.value shouldBe PlaybackState.Complete(player.totalEvents.value)

            player.destroy()
        }
    }

    test("step back reconstructs correct earlier state") {
        runTest {
            val player = AlgorithmPlayer()
            player.run(BubbleSortVisualizer(), listOf(3, 1, 2))

            // Step to end
            while (player.currentEventIndex.value < player.totalEvents.value - 1) {
                player.stepForward()
            }
            val endIndex = player.currentEventIndex.value

            // Step back
            player.stepBack()
            player.currentEventIndex.value shouldBe endIndex - 1
            player.state.value shouldBe PlaybackState.Paused

            player.destroy()
        }
    }

    test("stop resets to initial snapshot") {
        runTest {
            val player = AlgorithmPlayer()
            player.run(BubbleSortVisualizer(), listOf(3, 1, 2))

            // Walk forward a few steps
            player.stepForward()
            player.stepForward()

            player.stop()
            player.currentEventIndex.value shouldBe 0
            player.state.value shouldBe PlaybackState.Stopped

            player.destroy()
        }
    }

    test("BubbleSort on already sorted input only emits Start and Complete") {
        runTest {
            val player = AlgorithmPlayer()
            player.run(BubbleSortVisualizer(), listOf(1, 2, 3))

            // Already sorted: Start + 2 Compares (one pass, no swaps) + Complete
            player.totalEvents.value shouldBe 4

            // Step to end
            while (player.currentEventIndex.value < player.totalEvents.value - 1) {
                player.stepForward()
            }
            player.currentSnapshot.value.arrayState shouldBe listOf(1, 2, 3)

            player.destroy()
        }
    }
})
