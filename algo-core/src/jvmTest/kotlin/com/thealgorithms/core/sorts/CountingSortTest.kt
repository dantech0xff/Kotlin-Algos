package com.thealgorithms.core.sorts

import com.thealgorithms.core.utils.isSorted
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CountingSortTest : FunSpec({
    val sorter = CountingSort()

    test("sorts empty list") {
        sorter.sort(emptyList()) shouldBe emptyList()
    }

    test("sorts single element") {
        sorter.sort(listOf(42)) shouldBe listOf(42)
    }

    test("sorts already sorted list") {
        val input = listOf(1, 2, 3, 4, 5)
        val result = sorter.sort(input)
        result.isSorted() shouldBe true
        result shouldBe listOf(1, 2, 3, 4, 5)
    }

    test("sorts reverse sorted list") {
        val input = listOf(5, 4, 3, 2, 1)
        val result = sorter.sort(input)
        result.isSorted() shouldBe true
        result shouldBe listOf(1, 2, 3, 4, 5)
    }

    test("sorts list with duplicates") {
        val input = listOf(3, 1, 4, 1, 5, 9, 2, 6, 5)
        val result = sorter.sort(input)
        result.isSorted() shouldBe true
    }

    test("sorts random list") {
        val input = listOf(64, 34, 25, 12, 22, 11, 90)
        val result = sorter.sort(input)
        result.isSorted() shouldBe true
        result shouldBe listOf(11, 12, 22, 25, 34, 64, 90)
    }

    test("sorts list with negative numbers") {
        val input = listOf(3, -1, 4, -1, 5, -9, 2, 6, 0)
        val result = sorter.sort(input)
        result.isSorted() shouldBe true
        result shouldBe listOf(-9, -1, -1, 0, 2, 3, 4, 5, 6)
    }

    test("sorts all same elements") {
        val input = listOf(7, 7, 7, 7)
        val result = sorter.sort(input)
        result shouldBe listOf(7, 7, 7, 7)
    }

    test("sorts two element list") {
        sorter.sort(listOf(2, 1)) shouldBe listOf(1, 2)
    }
})
