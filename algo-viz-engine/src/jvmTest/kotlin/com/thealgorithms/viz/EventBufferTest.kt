package com.thealgorithms.viz

import com.thealgorithms.shared.AlgorithmEvent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EventBufferTest : StringSpec({
    "add and retrieve events" {
        val buffer = EventBuffer()
        val event1 = AlgorithmEvent.Compare(0 to 1)
        val event2 = AlgorithmEvent.Swap(0 to 1)

        buffer.add(event1)
        buffer.add(event2)

        buffer.size shouldBe 2
        buffer.get(0) shouldBe event1
        buffer.get(1) shouldBe event2
    }

    "getAll returns a copy of all events" {
        val buffer = EventBuffer()
        val events = listOf(
            AlgorithmEvent.Start(listOf(3, 1, 2)),
            AlgorithmEvent.Compare(0 to 1),
            AlgorithmEvent.Swap(0 to 1),
        )

        events.forEach { buffer.add(it) }

        buffer.getAll() shouldBe events
    }

    "capacity limit drops oldest events" {
        val buffer = EventBuffer(maxCapacity = 3)

        for (i in 0..4) {
            buffer.add(AlgorithmEvent.Select(i))
        }

        buffer.size shouldBe 3
        // Oldest (index 0, 1) should be dropped; remaining are index 2, 3, 4
        buffer.get(0) shouldBe AlgorithmEvent.Select(2)
        buffer.get(1) shouldBe AlgorithmEvent.Select(3)
        buffer.get(2) shouldBe AlgorithmEvent.Select(4)
    }

    "clear removes all events" {
        val buffer = EventBuffer()
        buffer.add(AlgorithmEvent.Start(listOf(1, 2)))
        buffer.add(AlgorithmEvent.Compare(0 to 1))

        buffer.size shouldBe 2
        buffer.clear()
        buffer.size shouldBe 0
    }

    "default capacity is 10000" {
        val buffer = EventBuffer()
        for (i in 0..9999) {
            buffer.add(AlgorithmEvent.Select(i))
        }
        buffer.size shouldBe 10_000

        buffer.add(AlgorithmEvent.Select(0))
        buffer.size shouldBe 10_000
        // Oldest dropped, first event is now index 1
        buffer.get(0) shouldBe AlgorithmEvent.Select(1)
    }
})
