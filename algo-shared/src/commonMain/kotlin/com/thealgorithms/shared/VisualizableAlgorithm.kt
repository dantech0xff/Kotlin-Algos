package com.thealgorithms.shared

import kotlinx.coroutines.flow.MutableSharedFlow

interface VisualizableAlgorithm {
    suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>)
}
