package com.thealgorithms.viz

import com.thealgorithms.shared.AlgorithmEvent

class EventBuffer(private val maxCapacity: Int = 10_000) {
    private val events = mutableListOf<AlgorithmEvent>()

    fun add(event: AlgorithmEvent) {
        if (events.size >= maxCapacity) {
            events.removeAt(0)
        }
        events.add(event)
    }

    fun getAll(): List<AlgorithmEvent> = events.toList()

    fun get(index: Int): AlgorithmEvent = events[index]

    val size: Int get() = events.size

    fun clear() = events.clear()
}
