package com.thealgorithms.viz.integration

import com.thealgorithms.core.searches.IterativeBinarySearchVisualizer
import com.thealgorithms.core.searches.LinearSearchVisualizer
import com.thealgorithms.core.searches.RecursiveBinarySearchVisualizer
import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SearchInput
import com.thealgorithms.shared.SearchVisualizableAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm
import com.thealgorithms.viz.AlgorithmPlayer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest

/**
 * Adapts a [SearchVisualizableAlgorithm] to the [VisualizableAlgorithm] interface
 * so it can run through [AlgorithmPlayer].
 */
class SearchAlgorithmAdapter(
    private val searchAlgo: SearchVisualizableAlgorithm,
    private val searchInput: SearchInput,
) : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        searchAlgo.execute(searchInput, emitter)
    }
}

class SearchIntegrationTest : FunSpec({

    test("LinearSearch through player finds element and preserves array") {
        runTest {
            val player = AlgorithmPlayer()
            val input = listOf(3, 1, 4, 1, 5)
            val adapter = SearchAlgorithmAdapter(
                LinearSearchVisualizer(),
                SearchInput(input, 4),
            )

            player.run(adapter, input)

            // Start + Probe(0) + Probe(1) + Probe(2) + Found(2) = 5 events
            player.totalEvents.value shouldBe 5

            // Step to end -- search does not mutate the array
            while (player.currentEventIndex.value < player.totalEvents.value - 1) {
                player.stepForward()
            }
            player.currentSnapshot.value.arrayState shouldBe input

            player.destroy()
        }
    }

    test("LinearSearch through player emits NotFound for missing key") {
        runTest {
            val player = AlgorithmPlayer()
            val input = listOf(1, 2, 3)
            val adapter = SearchAlgorithmAdapter(
                LinearSearchVisualizer(),
                SearchInput(input, 99),
            )

            player.run(adapter, input)

            // Start + Probe(0) + Probe(1) + Probe(2) + NotFound = 5 events
            player.totalEvents.value shouldBe 5

            player.destroy()
        }
    }

    test("IterativeBinarySearch through player emits RangeCheck and Probe events") {
        runTest {
            val player = AlgorithmPlayer()
            val sortedInput = listOf(1, 2, 3, 4, 5)
            val adapter = SearchAlgorithmAdapter(
                IterativeBinarySearchVisualizer(),
                SearchInput(sortedInput, 4),
            )

            player.run(adapter, sortedInput)

            // Start + RangeCheck + Probe + RangeCheck + Probe + Found = 6
            // First: RangeCheck(0,4), Probe(2) -> 4>3, left=3
            // Second: RangeCheck(3,4), Probe(3) -> found
            player.totalEvents.value shouldBe 6

            player.destroy()
        }
    }

    test("RecursiveBinarySearch through player finds element") {
        runTest {
            val player = AlgorithmPlayer()
            val sortedInput = listOf(1, 2, 3, 4, 5)
            val adapter = SearchAlgorithmAdapter(
                RecursiveBinarySearchVisualizer(),
                SearchInput(sortedInput, 3),
            )

            player.run(adapter, sortedInput)

            // Start + RangeCheck + Probe + Found = 4
            player.totalEvents.value shouldBe 4

            // Step to the last event -- should be Found(2)
            while (player.currentEventIndex.value < player.totalEvents.value - 1) {
                player.stepForward()
            }

            player.destroy()
        }
    }

    test("BinarySearch stepping forward and backward preserves array integrity") {
        runTest {
            val player = AlgorithmPlayer()
            val sortedInput = listOf(10, 20, 30, 40, 50)
            val adapter = SearchAlgorithmAdapter(
                IterativeBinarySearchVisualizer(),
                SearchInput(sortedInput, 30),
            )

            player.run(adapter, sortedInput)

            // Step forward
            player.stepForward()
            player.currentEventIndex.value shouldBe 1

            // Step back
            player.stepBack()
            player.currentEventIndex.value shouldBe 0

            // Array state should remain unchanged (searches don't mutate)
            player.currentSnapshot.value.arrayState shouldBe sortedInput

            player.destroy()
        }
    }
})
