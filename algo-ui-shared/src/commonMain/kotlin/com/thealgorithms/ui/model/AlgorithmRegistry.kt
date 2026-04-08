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
        // SORTING
        AlgorithmInfo(
            name = "Bubble Sort",
            category = AlgorithmCategory.SORTING,
            description = "O(n²) – Compares adjacent elements",
            visualizer = BubbleSortVisualizer(),
            difficulty = Difficulty.BEGINNER,
            timeComplexity = Complexity(best = "O(n)", average = "O(n²)", worst = "O(n²)"),
            spaceComplexity = "O(1)",
            isStable = true,
            tags = listOf("comparison-based", "in-place"),
            pseudocode = listOf(
                PseudocodeLine("function bubbleSort(arr):", 0, 1),
                PseudocodeLine("for i = 0 to n-2:", 1, 2),
                PseudocodeLine("for j = 0 to n-i-2:", 2, 3),
                PseudocodeLine("if arr[j] > arr[j+1]:", 2, 4),
                PseudocodeLine("swap(arr[j], arr[j+1])", 3, 5),
                PseudocodeLine("if no swaps: break", 1, 6),
            )
        ),
        AlgorithmInfo(
            name = "Selection Sort",
            category = AlgorithmCategory.SORTING,
            description = "O(n²) – Finds minimum each pass",
            visualizer = SelectionSortVisualizer(),
            difficulty = Difficulty.BEGINNER,
            timeComplexity = Complexity(best = "O(n²)", average = "O(n²)", worst = "O(n²)"),
            spaceComplexity = "O(1)",
            isStable = false,
            tags = listOf("comparison-based", "in-place"),
            pseudocode = listOf(
                PseudocodeLine("function selectionSort(arr):", 0, 1),
                PseudocodeLine("for i = 0 to n-2:", 1, 2),
                PseudocodeLine("minIdx = i", 2, 3),
                PseudocodeLine("for j = i+1 to n-1:", 2, 4),
                PseudocodeLine("if arr[j] < arr[minIdx]:", 3, 5),
                PseudocodeLine("minIdx = j", 4, 6),
                PseudocodeLine("swap(arr[i], arr[minIdx])", 2, 7),
            )
        ),
        AlgorithmInfo(
            name = "Insertion Sort",
            category = AlgorithmCategory.SORTING,
            description = "O(n²) – Inserts into sorted portion",
            visualizer = InsertionSortVisualizer(),
            difficulty = Difficulty.BEGINNER,
            timeComplexity = Complexity(best = "O(n)", average = "O(n²)", worst = "O(n²)"),
            spaceComplexity = "O(1)",
            isStable = true,
            tags = listOf("comparison-based", "in-place", "adaptive"),
            pseudocode = listOf(
                PseudocodeLine("function insertionSort(arr):", 0, 1),
                PseudocodeLine("for i = 1 to n-1:", 1, 2),
                PseudocodeLine("key = arr[i]", 2, 3),
                PseudocodeLine("j = i - 1", 2, 4),
                PseudocodeLine("while j >= 0 and arr[j] > key:", 2, 5),
                PseudocodeLine("arr[j+1] = arr[j]", 3, 6),
                PseudocodeLine("j = j - 1", 3, 7),
                PseudocodeLine("arr[j+1] = key", 2, 8),
            )
        ),
        AlgorithmInfo(
            name = "Quick Sort",
            category = AlgorithmCategory.SORTING,
            description = "O(n log n) avg – Divide and conquer",
            visualizer = QuickSortVisualizer(),
            difficulty = Difficulty.INTERMEDIATE,
            timeComplexity = Complexity(best = "O(n log n)", average = "O(n log n)", worst = "O(n²)"),
            spaceComplexity = "O(log n)",
            isStable = false,
            tags = listOf("divide-and-conquer", "comparison-based", "in-place"),
            pseudocode = listOf(
                PseudocodeLine("function quickSort(arr, lo, hi):", 0, 1),
                PseudocodeLine("if lo < hi:", 1, 2),
                PseudocodeLine("pivot = arr[hi]", 2, 3),
                PseudocodeLine("i = lo - 1", 2, 4),
                PseudocodeLine("for j = lo to hi-1:", 2, 5),
                PseudocodeLine("if arr[j] <= pivot:", 3, 6),
                PseudocodeLine("i = i + 1", 4, 7),
                PseudocodeLine("swap(arr[i], arr[j])", 4, 8),
                PseudocodeLine("swap(arr[i+1], arr[hi])", 2, 9),
                PseudocodeLine("quickSort(arr, lo, i)", 2, 10),
                PseudocodeLine("quickSort(arr, i+2, hi)", 2, 11),
            )
        ),
        AlgorithmInfo(
            name = "Merge Sort",
            category = AlgorithmCategory.SORTING,
            description = "O(n log n) – Divide, sort, merge",
            visualizer = MergeSortVisualizer(),
            difficulty = Difficulty.INTERMEDIATE,
            timeComplexity = Complexity(best = "O(n log n)", average = "O(n log n)", worst = "O(n log n)"),
            spaceComplexity = "O(n)",
            isStable = true,
            tags = listOf("divide-and-conquer", "comparison-based", "stable"),
            pseudocode = listOf(
                PseudocodeLine("function mergeSort(arr, l, r):", 0, 1),
                PseudocodeLine("if l < r:", 1, 2),
                PseudocodeLine("mid = (l + r) / 2", 2, 3),
                PseudocodeLine("mergeSort(arr, l, mid)", 2, 4),
                PseudocodeLine("mergeSort(arr, mid+1, r)", 2, 5),
                PseudocodeLine("merge(arr, l, mid, r)", 2, 6),
                PseudocodeLine("merge: compare & copy", 2, 7),
            )
        ),
        AlgorithmInfo(
            name = "Heap Sort",
            category = AlgorithmCategory.SORTING,
            description = "O(n log n) – Heapify and extract max",
            visualizer = HeapSortVisualizer(),
            difficulty = Difficulty.INTERMEDIATE,
            timeComplexity = Complexity(best = "O(n log n)", average = "O(n log n)", worst = "O(n log n)"),
            spaceComplexity = "O(1)",
            isStable = false,
            tags = listOf("comparison-based", "in-place", "heap"),
            pseudocode = listOf(
                PseudocodeLine("function heapSort(arr):", 0, 1),
                PseudocodeLine("build max heap", 1, 2),
                PseudocodeLine("for i = n-1 down to 1:", 1, 3),
                PseudocodeLine("swap(arr[0], arr[i])", 2, 4),
                PseudocodeLine("heapify(arr, 0, i)", 2, 5),
            )
        ),
        AlgorithmInfo(
            name = "Shell Sort",
            category = AlgorithmCategory.SORTING,
            description = "O(n^1.25) – Gap-based insertion",
            visualizer = ShellSortVisualizer(),
            difficulty = Difficulty.INTERMEDIATE,
            timeComplexity = Complexity(best = "O(n log n)", average = "O(n^1.25)", worst = "O(n²)"),
            spaceComplexity = "O(1)",
            isStable = false,
            tags = listOf("comparison-based", "in-place", "gap-sequence"),
            pseudocode = listOf(
                PseudocodeLine("function shellSort(arr):", 0, 1),
                PseudocodeLine("gap = n / 2", 1, 2),
                PseudocodeLine("while gap > 0:", 1, 3),
                PseudocodeLine("for i = gap to n-1:", 2, 4),
                PseudocodeLine("while j >= gap and arr[j-gap] > temp:", 3, 7),
                PseudocodeLine("arr[j] = arr[j-gap]", 4, 8),
                PseudocodeLine("arr[j] = temp", 3, 10),
                PseudocodeLine("gap = gap / 2", 2, 11),
            )
        ),
        AlgorithmInfo(
            name = "Counting Sort",
            category = AlgorithmCategory.SORTING,
            description = "O(n+k) – Non-comparison counting",
            visualizer = CountingSortVisualizer(),
            difficulty = Difficulty.INTERMEDIATE,
            timeComplexity = Complexity(best = "O(n+k)", average = "O(n+k)", worst = "O(n+k)"),
            spaceComplexity = "O(k)",
            isStable = true,
            tags = listOf("non-comparison", "integer-only"),
            pseudocode = listOf(
                PseudocodeLine("function countingSort(arr):", 0, 1),
                PseudocodeLine("find max value k", 1, 2),
                PseudocodeLine("create count[0..k] = 0", 1, 3),
                PseudocodeLine("count each element", 1, 4),
                PseudocodeLine("compute prefix sums", 1, 6),
                PseudocodeLine("place elements in output", 1, 9),
            )
        ),
        AlgorithmInfo(
            name = "Cocktail Sort",
            category = AlgorithmCategory.SORTING,
            description = "O(n²) – Bidirectional bubble",
            visualizer = CocktailSortVisualizer(),
            difficulty = Difficulty.BEGINNER,
            timeComplexity = Complexity(best = "O(n)", average = "O(n²)", worst = "O(n²)"),
            spaceComplexity = "O(1)",
            isStable = true,
            tags = listOf("comparison-based", "in-place", "bidirectional"),
            pseudocode = listOf(
                PseudocodeLine("function cocktailSort(arr):", 0, 1),
                PseudocodeLine("while swapped:", 1, 2),
                PseudocodeLine("forward pass:", 2, 3),
                PseudocodeLine("for i = start to end-1:", 3, 4),
                PseudocodeLine("if arr[i] > arr[i+1]:", 4, 5),
                PseudocodeLine("swap(arr[i], arr[i+1])", 5, 6),
                PseudocodeLine("backward pass:", 2, 7),
                PseudocodeLine("for i = end-1 down to start:", 3, 8),
                PseudocodeLine("if arr[i] > arr[i+1]:", 4, 9),
                PseudocodeLine("swap(arr[i], arr[i+1])", 5, 10),
            )
        ),
        AlgorithmInfo(
            name = "Cycle Sort",
            category = AlgorithmCategory.SORTING,
            description = "O(n²) – Minimal writes",
            visualizer = CycleSortVisualizer(),
            difficulty = Difficulty.ADVANCED,
            timeComplexity = Complexity(best = "O(n²)", average = "O(n²)", worst = "O(n²)"),
            spaceComplexity = "O(1)",
            isStable = false,
            tags = listOf("comparison-based", "in-place", "minimal-writes"),
            pseudocode = listOf(
                PseudocodeLine("function cycleSort(arr):", 0, 1),
                PseudocodeLine("for cycleStart = 0 to n-2:", 1, 2),
                PseudocodeLine("item = arr[cycleStart]", 2, 3),
                PseudocodeLine("find correct position", 2, 5),
                PseudocodeLine("swap item into position", 2, 8),
                PseudocodeLine("rotate rest of cycle", 2, 12),
            )
        ),
        AlgorithmInfo(
            name = "Radix Sort",
            category = AlgorithmCategory.SORTING,
            description = "O(d·n) – LSD digit-by-digit",
            visualizer = RadixSortVisualizer(),
            difficulty = Difficulty.INTERMEDIATE,
            timeComplexity = Complexity(best = "O(d·n)", average = "O(d·n)", worst = "O(d·n)"),
            spaceComplexity = "O(n+d)",
            isStable = true,
            tags = listOf("non-comparison", "integer-only", "LSD"),
            pseudocode = listOf(
                PseudocodeLine("function radixSort(arr):", 0, 1),
                PseudocodeLine("maxVal = max(arr)", 1, 2),
                PseudocodeLine("exp = 1", 1, 3),
                PseudocodeLine("while maxVal / exp > 0:", 1, 4),
                PseudocodeLine("countingSort by digit", 2, 5),
                PseudocodeLine("exp *= 10", 2, 6),
            )
        ),
        // SEARCHING
        AlgorithmInfo(
            name = "Linear Search",
            category = AlgorithmCategory.SEARCHING,
            description = "O(n) – Sequential scan",
            visualizer = LinearSearchVisualizer(),
            difficulty = Difficulty.BEGINNER,
            timeComplexity = Complexity(best = "O(1)", average = "O(n)", worst = "O(n)"),
            spaceComplexity = "O(1)",
            tags = listOf("sequential", "unsorted-ok"),
            pseudocode = listOf(
                PseudocodeLine("function linearSearch(arr, target):", 0, 1),
                PseudocodeLine("for i = 0 to n-1:", 1, 2),
                PseudocodeLine("if arr[i] == target:", 2, 3),
                PseudocodeLine("return i (found)", 3, 4),
                PseudocodeLine("return -1 (not found)", 1, 5),
            )
        ),
        AlgorithmInfo(
            name = "Binary Search (Iterative)",
            category = AlgorithmCategory.SEARCHING,
            description = "O(log n) – Halving search space",
            visualizer = IterativeBinarySearchVisualizer(),
            difficulty = Difficulty.BEGINNER,
            timeComplexity = Complexity(best = "O(1)", average = "O(log n)", worst = "O(log n)"),
            spaceComplexity = "O(1)",
            tags = listOf("divide-and-conquer", "requires-sorted"),
            pseudocode = listOf(
                PseudocodeLine("function binarySearch(arr, target):", 0, 1),
                PseudocodeLine("left = 0, right = n-1", 1, 2),
                PseudocodeLine("while left <= right:", 1, 3),
                PseudocodeLine("mid = (left + right) / 2", 2, 4),
                PseudocodeLine("if arr[mid] == target:", 2, 5),
                PseudocodeLine("return mid (found)", 3, 6),
                PseudocodeLine("else if arr[mid] < target:", 2, 7),
                PseudocodeLine("left = mid + 1", 3, 8),
                PseudocodeLine("else: right = mid - 1", 3, 9),
                PseudocodeLine("return -1 (not found)", 1, 10),
            )
        ),
        AlgorithmInfo(
            name = "Binary Search (Recursive)",
            category = AlgorithmCategory.SEARCHING,
            description = "O(log n) – Recursive halving",
            visualizer = RecursiveBinarySearchVisualizer(),
            difficulty = Difficulty.BEGINNER,
            timeComplexity = Complexity(best = "O(1)", average = "O(log n)", worst = "O(log n)"),
            spaceComplexity = "O(log n)",
            tags = listOf("divide-and-conquer", "requires-sorted", "recursive"),
            pseudocode = listOf(
                PseudocodeLine("function binarySearch(arr, target, l, r):", 0, 1),
                PseudocodeLine("if left > right:", 1, 2),
                PseudocodeLine("return -1 (not found)", 2, 3),
                PseudocodeLine("mid = (left + right) / 2", 1, 4),
                PseudocodeLine("if arr[mid] == target:", 1, 5),
                PseudocodeLine("return mid (found)", 2, 6),
                PseudocodeLine("else if arr[mid] < target:", 1, 7),
                PseudocodeLine("search(arr, target, mid+1, right)", 2, 8),
                PseudocodeLine("else: search(arr, target, left, mid-1)", 2, 9),
            )
        ),
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
