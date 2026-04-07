package com.thealgorithms.viz

import com.thealgorithms.shared.AlgorithmEvent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SnapshotReconstructorTest : StringSpec({
    val reconstructor = SnapshotReconstructor()

    "reconstruct from Start + Swap events" {
        val base = AlgorithmSnapshot(emptyList())
        val events = listOf(
            AlgorithmEvent.Start(listOf(3, 1, 2)),
            AlgorithmEvent.Swap(0 to 2),
        )

        val result = reconstructor.reconstruct(base, events)

        result.arrayState shouldBe listOf(2, 1, 3)
        result.highlightedIndices shouldBe setOf(0, 2)
        result.swaps shouldBe 1
    }

    "reconstruct from Start + Compare + Swap sequence" {
        val base = AlgorithmSnapshot(emptyList())
        val events = listOf(
            AlgorithmEvent.Start(listOf(5, 2, 8, 1)),
            AlgorithmEvent.Compare(0 to 1),
            AlgorithmEvent.Swap(0 to 1),
            AlgorithmEvent.Compare(1 to 2),
        )

        val result = reconstructor.reconstruct(base, events)

        result.arrayState shouldBe listOf(2, 5, 8, 1)
        result.comparisons shouldBe 2
        result.swaps shouldBe 1
        result.highlightedIndices shouldBe setOf(1, 2)
    }

    "reconstruct from Overwrite events" {
        val base = AlgorithmSnapshot(listOf(0, 0, 0))
        val events = listOf(
            AlgorithmEvent.Overwrite(0, 10),
            AlgorithmEvent.Overwrite(1, 20),
            AlgorithmEvent.Overwrite(2, 30),
        )

        val result = reconstructor.reconstruct(base, events)

        result.arrayState shouldBe listOf(10, 20, 30)
        result.highlightedIndices shouldBe setOf(2)
    }

    "empty events list returns same snapshot" {
        val snapshot = AlgorithmSnapshot(listOf(1, 2, 3), setOf(0), 5, 2)

        val result = reconstructor.reconstruct(snapshot, emptyList())

        result shouldBe snapshot
    }

    "reconstruct Complete event sets final state" {
        val base = AlgorithmSnapshot(listOf(1, 2, 3))
        val events = listOf(
            AlgorithmEvent.Complete(listOf(1, 2, 3)),
        )

        val result = reconstructor.reconstruct(base, events)

        result.arrayState shouldBe listOf(1, 2, 3)
        result.highlightedIndices shouldBe emptySet()
    }

    "reconstruct handles Select and Deselect" {
        val base = AlgorithmSnapshot(listOf(5, 3, 1))
        val events = listOf(
            AlgorithmEvent.Select(1),
            AlgorithmEvent.Deselect(1),
        )

        val result = reconstructor.reconstruct(base, events)

        result.highlightedIndices shouldBe emptySet()
        result.arrayState shouldBe listOf(5, 3, 1)
    }

    "reconstruct incremental from a mid-point snapshot" {
        val base = AlgorithmSnapshot(listOf(2, 5, 8, 1), comparisons = 2, swaps = 1)
        val events = listOf(
            AlgorithmEvent.Swap(2 to 3),
        )

        val result = reconstructor.reconstruct(base, events)

        result.arrayState shouldBe listOf(2, 5, 1, 8)
        result.swaps shouldBe 2
        result.highlightedIndices shouldBe setOf(2, 3)
    }

    "reconstruct Probe and Found events for search" {
        val base = AlgorithmSnapshot(listOf(10, 20, 30, 40))
        val events = listOf(
            AlgorithmEvent.Probe(0),
            AlgorithmEvent.Probe(1),
            AlgorithmEvent.Found(1),
        )

        val result = reconstructor.reconstruct(base, events)

        result.highlightedIndices shouldBe setOf(1)
        result.arrayState shouldBe listOf(10, 20, 30, 40)
    }
})
