package com.thealgorithms.ui.model

import com.thealgorithms.core.searches.IterativeBinarySearchVisualizer
import com.thealgorithms.core.searches.LinearSearchVisualizer
import com.thealgorithms.core.searches.RecursiveBinarySearchVisualizer
import com.thealgorithms.core.sorts.BubbleSortVisualizer
import com.thealgorithms.core.sorts.CocktailSortVisualizer
import com.thealgorithms.core.sorts.CountingSortVisualizer
import com.thealgorithms.core.sorts.CycleSortVisualizer
import com.thealgorithms.core.sorts.HeapSortVisualizer
import com.thealgorithms.core.sorts.InsertionSortVisualizer
import com.thealgorithms.core.sorts.MergeSortVisualizer
import com.thealgorithms.core.sorts.QuickSortVisualizer
import com.thealgorithms.core.sorts.RadixSortVisualizer
import com.thealgorithms.core.sorts.SelectionSortVisualizer
import com.thealgorithms.core.sorts.ShellSortVisualizer
import com.thealgorithms.shared.SearchVisualizableAlgorithm
import com.thealgorithms.shared.VisualizableAlgorithm

object AlgorithmRegistry {
    val algorithms = listOf(
        AlgorithmInfo("Bubble Sort", AlgorithmCategory.SORTING, "O(n\u00B2) \u2013 Compares adjacent elements", BubbleSortVisualizer()),
        AlgorithmInfo("Selection Sort", AlgorithmCategory.SORTING, "O(n\u00B2) \u2013 Finds minimum each pass", SelectionSortVisualizer()),
        AlgorithmInfo("Insertion Sort", AlgorithmCategory.SORTING, "O(n\u00B2) \u2013 Inserts into sorted portion", InsertionSortVisualizer()),
        AlgorithmInfo("Quick Sort", AlgorithmCategory.SORTING, "O(n log n) avg \u2013 Divide and conquer", QuickSortVisualizer()),
        AlgorithmInfo("Merge Sort", AlgorithmCategory.SORTING, "O(n log n) \u2013 Divide, sort, merge", MergeSortVisualizer()),
        AlgorithmInfo("Heap Sort", AlgorithmCategory.SORTING, "O(n log n) \u2013 Heapify and extract max", HeapSortVisualizer()),
        AlgorithmInfo("Shell Sort", AlgorithmCategory.SORTING, "O(n^1.25) \u2013 Gap-based insertion", ShellSortVisualizer()),
        AlgorithmInfo("Counting Sort", AlgorithmCategory.SORTING, "O(n+k) \u2013 Non-comparison counting", CountingSortVisualizer()),
        AlgorithmInfo("Cocktail Sort", AlgorithmCategory.SORTING, "O(n\u00B2) \u2013 Bidirectional bubble", CocktailSortVisualizer()),
        AlgorithmInfo("Cycle Sort", AlgorithmCategory.SORTING, "O(n\u00B2) \u2013 Minimal writes", CycleSortVisualizer()),
        AlgorithmInfo("Radix Sort", AlgorithmCategory.SORTING, "O(d\u00B7n) \u2013 LSD digit-by-digit", RadixSortVisualizer()),
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
