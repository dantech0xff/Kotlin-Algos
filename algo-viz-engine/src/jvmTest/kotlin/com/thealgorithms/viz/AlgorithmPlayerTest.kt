package com.thealgorithms.viz

import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.PlaybackState
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class AlgorithmPlayerTest : StringSpec({

    "run collects all events from mock algorithm" {
        runTest {
            val events = listOf(
                AlgorithmEvent.Start(listOf(3, 1, 2)),
                AlgorithmEvent.Compare(0 to 1),
                AlgorithmEvent.Swap(0 to 1),
                AlgorithmEvent.Complete(listOf(1, 3, 2)),
            )
            val player = AlgorithmPlayer(snapshotInterval = 2)
            val algorithm = MockVisualizableAlgorithm(events)

            player.run(algorithm, listOf(3, 1, 2))

            player.totalEvents.value shouldBe 4
            player.destroy()
        }
    }

    "run sets initial snapshot from first event" {
        runTest {
            val events = listOf(
                AlgorithmEvent.Start(listOf(5, 2)),
                AlgorithmEvent.Compare(0 to 1),
                AlgorithmEvent.Complete(listOf(2, 5)),
            )
            val player = AlgorithmPlayer()
            val algorithm = MockVisualizableAlgorithm(events)

            player.run(algorithm, listOf(5, 2))

            player.currentSnapshot.value.arrayState shouldBe listOf(5, 2)
            player.destroy()
        }
    }

    "stepForward advances event index and updates snapshot" {
        runTest {
            val events = listOf(
                AlgorithmEvent.Start(listOf(3, 1, 2)),
                AlgorithmEvent.Compare(0 to 1),
                AlgorithmEvent.Swap(0 to 1),
                AlgorithmEvent.Complete(listOf(1, 3, 2)),
            )
            val player = AlgorithmPlayer()
            val algorithm = MockVisualizableAlgorithm(events)

            player.run(algorithm, listOf(3, 1, 2))

            player.currentEventIndex.value shouldBe 0
            player.stepForward()
            player.currentEventIndex.value shouldBe 1
            player.currentSnapshot.value.comparisons shouldBe 1
            player.destroy()
        }
    }

    "stepBack decrements event index" {
        runTest {
            val events = listOf(
                AlgorithmEvent.Start(listOf(3, 1, 2)),
                AlgorithmEvent.Compare(0 to 1),
                AlgorithmEvent.Swap(0 to 1),
            )
            val player = AlgorithmPlayer()
            val algorithm = MockVisualizableAlgorithm(events)

            player.run(algorithm, listOf(3, 1, 2))

            player.stepForward()
            player.stepForward()
            player.currentEventIndex.value shouldBe 2

            player.stepBack()
            player.currentEventIndex.value shouldBe 1
            player.state.value shouldBe PlaybackState.Paused
            player.destroy()
        }
    }

    "stop resets to initial state" {
        runTest {
            val events = listOf(
                AlgorithmEvent.Start(listOf(3, 1, 2)),
                AlgorithmEvent.Compare(0 to 1),
                AlgorithmEvent.Swap(0 to 1),
            )
            val player = AlgorithmPlayer()
            val algorithm = MockVisualizableAlgorithm(events)

            player.run(algorithm, listOf(3, 1, 2))

            player.stepForward()
            player.stepForward()
            player.currentEventIndex.value shouldBe 2

            player.stop()
            player.currentEventIndex.value shouldBe 0
            player.state.value shouldBe PlaybackState.Stopped
            player.destroy()
        }
    }

    "stepForward at last event transitions to Complete" {
        runTest {
            val events = listOf(
                AlgorithmEvent.Start(listOf(1, 2)),
                AlgorithmEvent.Compare(0 to 1),
                AlgorithmEvent.Complete(listOf(1, 2)),
            )
            val player = AlgorithmPlayer()
            val algorithm = MockVisualizableAlgorithm(events)

            player.run(algorithm, listOf(1, 2))

            player.stepForward() // index 1
            player.stepForward() // index 2 = last

            player.state.value shouldBe PlaybackState.Complete(3)
            player.destroy()
        }
    }

    "stepBack at index 0 is a no-op" {
        runTest {
            val events = listOf(
                AlgorithmEvent.Start(listOf(1, 2)),
                AlgorithmEvent.Complete(listOf(1, 2)),
            )
            val player = AlgorithmPlayer()
            val algorithm = MockVisualizableAlgorithm(events)

            player.run(algorithm, listOf(1, 2))

            player.currentEventIndex.value shouldBe 0

            player.stepBack()
            player.currentEventIndex.value shouldBe 0
            player.destroy()
        }
    }

    "setSpeed validates range" {
        val player = AlgorithmPlayer()

        player.setSpeed(500L) // valid

        try {
            player.setSpeed(5L) // too low
            throw AssertionError("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }

        try {
            player.setSpeed(3000L) // too high
            throw AssertionError("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }

        player.destroy()
    }

    "play and pause cycle works" {
        runTest {
            val events = listOf(
                AlgorithmEvent.Start(listOf(3, 1, 2)),
                AlgorithmEvent.Compare(0 to 1),
                AlgorithmEvent.Swap(0 to 1),
                AlgorithmEvent.Complete(listOf(1, 3, 2)),
            )
            val player = AlgorithmPlayer()
            val algorithm = MockVisualizableAlgorithm(events)

            player.run(algorithm, listOf(3, 1, 2))

            player.play()

            player.state.value shouldBe PlaybackState.Playing

            player.pause()
            player.state.value shouldBe PlaybackState.Paused

            player.destroy()
        }
    }
})
