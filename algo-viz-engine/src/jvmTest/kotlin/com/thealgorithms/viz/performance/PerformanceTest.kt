package com.thealgorithms.viz.performance

import com.thealgorithms.core.sorts.BubbleSortVisualizer
import com.thealgorithms.core.sorts.InsertionSortVisualizer
import com.thealgorithms.core.sorts.MergeSortVisualizer
import com.thealgorithms.core.sorts.QuickSortVisualizer
import com.thealgorithms.core.sorts.SelectionSortVisualizer
import com.thealgorithms.viz.AlgorithmPlayer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.longs.shouldBeLessThan
import kotlinx.coroutines.test.runTest
import kotlin.system.measureTimeMillis

class PerformanceTest : FunSpec({

    test("BubbleSort on 200 elements completes event capture in under 5 seconds") {
        runTest {
            val input = (200 downTo 1).toList()
            val player = AlgorithmPlayer()

            val time = measureTimeMillis {
                player.run(BubbleSortVisualizer(), input)
            }

            time shouldBeLessThan 5000L
            // BubbleSort on 200 reversed elements produces thousands of events
            (player.totalEvents.value > 200) shouldBe true

            player.destroy()
        }
    }

    test("MergeSort on 500 elements completes event capture in under 5 seconds") {
        runTest {
            val input = (500 downTo 1).toList()
            val player = AlgorithmPlayer()

            val time = measureTimeMillis {
                player.run(MergeSortVisualizer(), input)
            }

            time shouldBeLessThan 5000L

            player.destroy()
        }
    }

    test("QuickSort on 500 elements completes event capture in under 5 seconds") {
        runTest {
            val input = (500 downTo 1).toList()
            val player = AlgorithmPlayer()

            val time = measureTimeMillis {
                player.run(QuickSortVisualizer(), input)
            }

            time shouldBeLessThan 5000L

            player.destroy()
        }
    }

    test("SelectionSort on 200 elements completes event capture in under 5 seconds") {
        runTest {
            val input = (200 downTo 1).toList()
            val player = AlgorithmPlayer()

            val time = measureTimeMillis {
                player.run(SelectionSortVisualizer(), input)
            }

            time shouldBeLessThan 5000L

            player.destroy()
        }
    }

    test("InsertionSort on 200 elements completes event capture in under 5 seconds") {
        runTest {
            val input = (200 downTo 1).toList()
            val player = AlgorithmPlayer()

            val time = measureTimeMillis {
                player.run(InsertionSortVisualizer(), input)
            }

            time shouldBeLessThan 5000L

            player.destroy()
        }
    }

    test("step forward through 200-element BubbleSort completes in under 3 seconds") {
        runTest {
            val input = (200 downTo 1).toList()
            val player = AlgorithmPlayer()
            player.run(BubbleSortVisualizer(), input)

            val time = measureTimeMillis {
                while (player.currentEventIndex.value < player.totalEvents.value - 1) {
                    player.stepForward()
                }
            }

            time shouldBeLessThan 3000L
            player.currentSnapshot.value.arrayState.size shouldBe 200

            player.destroy()
        }
    }
})
