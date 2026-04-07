package com.thealgorithms.shared

import kotlinx.coroutines.flow.MutableSharedFlow

interface SearchVisualizableAlgorithm {
    suspend fun execute(input: SearchInput, emitter: MutableSharedFlow<AlgorithmEvent>)
}
