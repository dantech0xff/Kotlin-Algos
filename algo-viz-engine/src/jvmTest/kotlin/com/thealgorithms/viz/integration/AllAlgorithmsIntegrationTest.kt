package com.thealgorithms.viz.integration

import com.thealgorithms.core.searches.IterativeBinarySearchVisualizer
import com.thealgorithms.core.searches.LinearSearchVisualizer
import com.thealgorithms.core.searches.RecursiveBinarySearchVisualizer
import com.thealgorithms.core.sorts.BubbleSortVisualizer
import com.thealgorithms.core.sorts.InsertionSortVisualizer
import com.thealgorithms.core.sorts.MergeSortVisualizer
import com.thealgorithms.core.sorts.QuickSortVisualizer
import com.thealgorithms.core.sorts.SelectionSortVisualizer
import com.thealgorithms.shared.SearchInput
import com.thealgorithms.shared.VisualizableAlgorithm
import com.thealgorithms.viz.AlgorithmPlayer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class AllAlgorithmsIntegrationTest : FunSpec({

    //region Sort algorithms -- all produce sorted output through the player

    val sortVisualizers = mapOf<String, VisualizableAlgorithm>(
        "BubbleSort" to BubbleSortVisualizer(),
        "SelectionSort" to SelectionSortVisualizer(),
        "InsertionSort" to InsertionSortVisualizer(),
        "QuickSort" to QuickSortVisualizer(),
        "MergeSort" to MergeSortVisualizer(),
    )

    sortVisualizers.forEach { (name, visualizer) ->
        test("$name produces sorted output through player") {
            runTest {
                val player = AlgorithmPlayer()
                val input = listOf(5, 3, 1, 4, 2)

                player.run(visualizer, input)

                // Must have at least Start + Complete (typically many more)
                (player.totalEvents.value >= 2) shouldBe true

                // Step to end
                while (player.currentEventIndex.value < player.totalEvents.value - 1) {
                    player.stepForward()
                }

                player.currentSnapshot.value.arrayState shouldBe listOf(1, 2, 3, 4, 5)

                player.destroy()
            }
        }

        test("$name produces more than just Start and Complete for unsorted input") {
            runTest {
                val player = AlgorithmPlayer()
                player.run(visualizer, listOf(2, 1))

                // At minimum Start + some operation(s) + Complete = at least 3
                (player.totalEvents.value >= 3) shouldBe true

                player.destroy()
            }
        }

        test("$name handles single-element input") {
            runTest {
                val player = AlgorithmPlayer()
                player.run(visualizer, listOf(42))

                player.currentSnapshot.value.arrayState shouldBe listOf(42)

                player.destroy()
            }
        }

        test("$name handles empty input without crashing") {
            runTest {
                val player = AlgorithmPlayer()
                player.run(visualizer, emptyList())

                // Should not crash; Start + Complete at minimum
                (player.totalEvents.value >= 2) shouldBe true

                player.destroy()
            }
        }
    }

    //endregion

    //region Search algorithms -- all produce events and preserve array

    val searchVisualizerMap = mapOf(
        "LinearSearch" to LinearSearchVisualizer(),
        "IterativeBinarySearch" to IterativeBinarySearchVisualizer(),
        "RecursiveBinarySearch" to RecursiveBinarySearchVisualizer(),
    )

    searchVisualizerMap.forEach { (name, visualizer) ->
        test("$name produces events through player with SearchAlgorithmAdapter") {
            runTest {
                val input = listOf(1, 2, 3, 4, 5)
                val adapter = SearchAlgorithmAdapter(
                    visualizer,
                    SearchInput(input, 3),
                )
                val player = AlgorithmPlayer()

                player.run(adapter, input)

                // At least Start + some probe/search event
                (player.totalEvents.value >= 2) shouldBe true

                player.destroy()
            }
        }

        test("$name does not mutate the input array") {
            runTest {
                val input = listOf(2, 4, 6, 8, 10)
                val adapter = SearchAlgorithmAdapter(
                    visualizer,
                    SearchInput(input, 6),
                )
                val player = AlgorithmPlayer()

                player.run(adapter, input)

                // Step to end
                while (player.currentEventIndex.value < player.totalEvents.value - 1) {
                    player.stepForward()
                }

                player.currentSnapshot.value.arrayState shouldBe input

                player.destroy()
            }
        }
    }

    //endregion
})
