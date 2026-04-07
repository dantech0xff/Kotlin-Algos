package com.thealgorithms.ui.model

import com.thealgorithms.core.searches.IterativeBinarySearchVisualizer
import com.thealgorithms.core.searches.LinearSearchVisualizer
import com.thealgorithms.core.searches.RecursiveBinarySearchVisualizer
import com.thealgorithms.core.sorts.BubbleSortVisualizer
import com.thealgorithms.core.sorts.InsertionSortVisualizer
import com.thealgorithms.core.sorts.MergeSortVisualizer
import com.thealgorithms.core.sorts.QuickSortVisualizer
import com.thealgorithms.core.sorts.SelectionSortVisualizer
import com.thealgorithms.shared.SearchVisualizableAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm

enum class AlgorithmCategory { SORTING, SEARCHING }

data class AlgorithmInfo(
    val name: String,
    val category: AlgorithmCategory,
    val description: String,
    val visualizer: Any
)

object AlgorithmRegistry {
    val algorithms = listOf(
        AlgorithmInfo("Bubble Sort", AlgorithmCategory.SORTING, "O(n\u00B2) \u2013 Compares adjacent elements", BubbleSortVisualizer()),
        AlgorithmInfo("Selection Sort", AlgorithmCategory.SORTING, "O(n\u00B2) \u2013 Finds minimum each pass", SelectionSortVisualizer()),
        AlgorithmInfo("Insertion Sort", AlgorithmCategory.SORTING, "O(n\u00B2) \u2013 Inserts into sorted portion", InsertionSortVisualizer()),
        AlgorithmInfo("Quick Sort", AlgorithmCategory.SORTING, "O(n log n) avg \u2013 Divide and conquer", QuickSortVisualizer()),
        AlgorithmInfo("Merge Sort", AlgorithmCategory.SORTING, "O(n log n) \u2013 Divide, sort, merge", MergeSortVisualizer()),
        AlgorithmInfo("Linear Search", AlgorithmCategory.SEARCHING, "O(n) \u2013 Sequential scan", LinearSearchVisualizer()),
        AlgorithmInfo("Binary Search (Iterative)", AlgorithmCategory.SEARCHING, "O(log n) \u2013 Halving search space", IterativeBinarySearchVisualizer()),
        AlgorithmInfo("Binary Search (Recursive)", AlgorithmCategory.SEARCHING, "O(log n) \u2013 Recursive halving", RecursiveBinarySearchVisualizer()),
    )

    val sortAlgorithms = algorithms.filter { it.category == AlgorithmCategory.SORTING }
    val searchAlgorithms = algorithms.filter { it.category == AlgorithmCategory.SEARCHING }

    fun visualizerAsSort(info: AlgorithmInfo): VisualizableAlgorithm {
        require(info.category == AlgorithmCategory.SORTING)
        return info.visualizer as VisualizableAlgorithm
    }

    fun visualizerAsSearch(info: AlgorithmInfo): SearchVisualizableAlgorithm {
        require(info.category == AlgorithmCategory.SEARCHING)
        return info.visualizer as SearchVisualizableAlgorithm
    }
}
