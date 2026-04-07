package com.thealgorithms.viz

import com.thealgorithms.shared.AlgorithmEvent
import com.thealgorithms.shared.VisualizableAlgorithm
import kotlinx.coroutines.flow.MutableSharedFlow

class MockVisualizableAlgorithm(
    private val events: List<AlgorithmEvent>
) : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        for (event in events) {
            emitter.emit(event)
        }
    }
}
