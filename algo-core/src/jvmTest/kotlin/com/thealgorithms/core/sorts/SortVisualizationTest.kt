package com.thealgorithms.core.sorts

import com.thealgorithms.shared.AlgorithmEvent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow

class SortVisualizationTest : FunSpec({

    test("BubbleSortVisualizer emits Start and Complete with sorted result") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        BubbleSortVisualizer().execute(listOf(3, 1, 2), emitter)
        val events = emitter.replayCache

        (events.first() as AlgorithmEvent.Start).data shouldBe listOf(3, 1, 2)
        (events.last() as AlgorithmEvent.Complete).result shouldBe listOf(1, 2, 3)
        // Start + Compare(0,1) + Swap(0,1) + Compare(1,2) + Swap(1,2) + Compare(0,1) + Complete
        events.shouldHaveSize(7)
    }

    test("SelectionSortVisualizer emits Start and Complete with sorted result") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        SelectionSortVisualizer().execute(listOf(3, 1, 2), emitter)
        val events = emitter.replayCache

        (events.first() as AlgorithmEvent.Start).data shouldBe listOf(3, 1, 2)
        (events.last() as AlgorithmEvent.Complete).result shouldBe listOf(1, 2, 3)
    }

    test("InsertionSortVisualizer emits Start and Complete with sorted result") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        InsertionSortVisualizer().execute(listOf(3, 1, 2), emitter)
        val events = emitter.replayCache

        (events.first() as AlgorithmEvent.Start).data shouldBe listOf(3, 1, 2)
        (events.last() as AlgorithmEvent.Complete).result shouldBe listOf(1, 2, 3)
    }

    test("QuickSortVisualizer emits Start and Complete with sorted result") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        QuickSortVisualizer().execute(listOf(3, 1, 2), emitter)
        val events = emitter.replayCache

        (events.first() as AlgorithmEvent.Start).data shouldBe listOf(3, 1, 2)
        (events.last() as AlgorithmEvent.Complete).result shouldBe listOf(1, 2, 3)
    }

    test("MergeSortVisualizer emits Start and Complete with sorted result") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        MergeSortVisualizer().execute(listOf(3, 1, 2), emitter)
        val events = emitter.replayCache

        (events.first() as AlgorithmEvent.Start).data shouldBe listOf(3, 1, 2)
        (events.last() as AlgorithmEvent.Complete).result shouldBe listOf(1, 2, 3)
    }
})
