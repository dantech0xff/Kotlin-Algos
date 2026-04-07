package com.thealgorithms.core.searches

import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.SearchInput
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow

class SearchVisualizationTest : FunSpec({

    test("LinearSearch emits Start, Probes, then Found") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        LinearSearchVisualizer()
            .execute(SearchInput(listOf(3, 1, 4, 1, 5), 4), emitter)
        val events = emitter.replayCache

        events.filterIsInstance<AlgorithmEvent.Start>().size shouldBe 1
        events.filterIsInstance<AlgorithmEvent.Probe>() shouldBe
            listOf(
                AlgorithmEvent.Probe(0),
                AlgorithmEvent.Probe(1),
                AlgorithmEvent.Probe(2),
            )
        events.last() shouldBe AlgorithmEvent.Found(2)
    }

    test("LinearSearch emits NotFound when key absent") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        LinearSearchVisualizer()
            .execute(SearchInput(listOf(1, 2, 3), 99), emitter)
        val events = emitter.replayCache

        events.last() shouldBe AlgorithmEvent.NotFound
    }

    test("IterativeBinarySearch emits RangeCheck events showing narrowing range") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        IterativeBinarySearchVisualizer()
            .execute(SearchInput(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 7), emitter)
        val events = emitter.replayCache

        val rangeChecks = events.filterIsInstance<AlgorithmEvent.RangeCheck>()
        rangeChecks.shouldNotBeEmpty()
        rangeChecks.first().low shouldBe 0
        rangeChecks.first().high shouldBe 9
        events.last() shouldBe AlgorithmEvent.Found(6)
    }

    test("IterativeBinarySearch emits NotFound when key absent") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        IterativeBinarySearchVisualizer()
            .execute(SearchInput(listOf(1, 3, 5, 7), 4), emitter)
        val events = emitter.replayCache

        events.last() shouldBe AlgorithmEvent.NotFound
    }

    test("RecursiveBinarySearch emits RangeCheck events and Found") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        RecursiveBinarySearchVisualizer()
            .execute(SearchInput(listOf(2, 4, 6, 8, 10), 8), emitter)
        val events = emitter.replayCache

        val rangeChecks = events.filterIsInstance<AlgorithmEvent.RangeCheck>()
        rangeChecks.shouldNotBeEmpty()
        events.last() shouldBe AlgorithmEvent.Found(3)
    }

    test("RecursiveBinarySearch emits NotFound when key absent") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        RecursiveBinarySearchVisualizer()
            .execute(SearchInput(listOf(1, 2, 3), 99), emitter)
        val events = emitter.replayCache

        events.last() shouldBe AlgorithmEvent.NotFound
    }
})
