package com.thealgorithms.core

import com.thealgorithms.core.utils.isGreaterThan
import com.thealgorithms.core.utils.isLessThan
import com.thealgorithms.core.utils.isSorted
import com.thealgorithms.core.utils.swapAt
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SortUtilsTest : StringSpec({
    "swapAt swaps two elements" {
        val list = mutableListOf(1, 2, 3, 4, 5)
        list.swapAt(0, 4)
        list shouldBe mutableListOf(5, 2, 3, 4, 1)
    }

    "swapAt is no-op when i equals j" {
        val list = mutableListOf(10, 20, 30)
        list.swapAt(1, 1)
        list shouldBe mutableListOf(10, 20, 30)
    }

    "isLessThan returns correct comparison" {
        1.isLessThan(2) shouldBe true
        2.isLessThan(1) shouldBe false
        1.isLessThan(1) shouldBe false
    }

    "isGreaterThan returns correct comparison" {
        5.isGreaterThan(3) shouldBe true
        3.isGreaterThan(5) shouldBe false
        3.isGreaterThan(3) shouldBe false
    }

    "isSorted returns true for sorted list" {
        listOf(1, 2, 3, 4, 5).isSorted() shouldBe true
    }

    "isSorted returns false for unsorted list" {
        listOf(3, 1, 4, 1, 5).isSorted() shouldBe false
    }

    "isSorted returns true for empty list" {
        emptyList<Int>().isSorted() shouldBe true
    }

    "isSorted returns true for single-element list" {
        listOf(42).isSorted() shouldBe true
    }
})
