package com.thealgorithms.core.searches

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class IterativeBinarySearchTest : FunSpec({
    val searcher = IterativeBinarySearch()

    test("returns -1 for empty list") {
        searcher.find(emptyList<Int>(), 1) shouldBe -1
    }

    test("finds element in single-element list") {
        searcher.find(listOf(42), 42) shouldBe 0
    }

    test("returns -1 when single element does not match") {
        searcher.find(listOf(42), 99) shouldBe -1
    }

    test("finds first element") {
        searcher.find(listOf(1, 2, 3, 4, 5), 1) shouldBe 0
    }

    test("finds last element") {
        searcher.find(listOf(1, 2, 3, 4, 5), 5) shouldBe 4
    }

    test("finds middle element") {
        searcher.find(listOf(1, 2, 3, 4, 5), 3) shouldBe 2
    }

    test("returns -1 when element not present") {
        searcher.find(listOf(1, 2, 3, 4, 5), 99) shouldBe -1
    }

    test("finds element in even-length list") {
        searcher.find(listOf(2, 4, 6, 8, 10, 12), 8) shouldBe 3
    }

    test("works with strings") {
        searcher.find(listOf("apple", "banana", "cherry"), "banana") shouldBe 1
    }
})
