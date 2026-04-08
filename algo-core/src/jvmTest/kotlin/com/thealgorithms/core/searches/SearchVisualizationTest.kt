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
        val probes = events.filterIsInstance<AlgorithmEvent.Probe>()
        probes.map { it.index } shouldBe listOf(0, 1, 2)
        probes.all { it.description.isNotEmpty() } shouldBe true
        val found = events.last()
        found shouldBe AlgorithmEvent.Found(2, description = (found as AlgorithmEvent.Found).description, pseudocodeLine = (found as AlgorithmEvent.Found).pseudocodeLine)
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
        val found = events.filterIsInstance<AlgorithmEvent.Found>().last()
        found.index shouldBe 6
        found.description.isNotEmpty() shouldBe true
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
        val found = events.filterIsInstance<AlgorithmEvent.Found>().last()
        found.index shouldBe 3
        found.description.isNotEmpty() shouldBe true
    }

    test("RecursiveBinarySearch emits NotFound when key absent") {
        val emitter = MutableSharedFlow<AlgorithmEvent>(replay = Int.MAX_VALUE)
        RecursiveBinarySearchVisualizer()
            .execute(SearchInput(listOf(1, 2, 3), 99), emitter)
        val events = emitter.replayCache

        events.last() shouldBe AlgorithmEvent.NotFound
    }
})
