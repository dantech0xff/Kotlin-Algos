# Phase 02: Algorithm Descriptions — All 14 Algorithms

## Context
- Parent: [plan.md](./plan.md)
- Depends on: [phase-01-foundation.md](./phase-01-foundation.md) (event API must be extended first)
- Brainstorm: [brainstorm-260408-educational-excellence.md](../reports/brainstorm-260408-educational-excellence.md)

## Overview
- **Date**: 2026-04-08
- **Priority**: P1
- **Status**: Completed
- **Effort**: 3h
- **Can run in parallel with**: Phase 03

Add human-readable descriptions to every event emission in all 14 algorithm visualizers. Update AlgorithmRegistry with rich metadata (complexity, stability, difficulty, tags, pseudocode).

## Key Insights
- Each visualizer emits events with indices but NO descriptions — students see colored bars but don't know WHY
- Descriptions should reference actual array values (e.g., "Comparing arr[2]=7 with arr[3]=2") — emitters have access to the array
- Pseudocode should be generic (not Kotlin) — more universal for students
- Each event should map to a pseudocode line for highlighting
- 11 sort files + 3 search files = 14 files to update

## Requirements
- Every `Compare`, `Swap`, `Pivot`, `Overwrite`, `Probe`, `Found`, `RangeCheck` event gets a descriptive string
- Every event gets a `pseudocodeLine` pointing to the relevant pseudocode line number
- AlgorithmRegistry entries get full metadata
- Descriptions reference actual values from the array at emission time

## Architecture

### Description Helper (DRY)
Create a utility to avoid repetitive string formatting:

```kotlin
// algo-core/.../utils/DescriptionUtils.kt
object DescriptionUtils {
    fun compare(i: Int, j: Int, arr: List<Int>): String =
        "Comparing arr[$i]=${arr[i]} with arr[$j]=${arr[j]}"

    fun swap(i: Int, j: Int, arr: List<Int>): String =
        "Since ${arr[i]} > ${arr[j]}, swap positions $i and $j"

    fun noSwap(i: Int, j: Int, arr: List<Int>): String =
        "${arr[i]} ≤ ${arr[j]}, no swap needed"

    fun pivot(index: Int, arr: List<Int>): String =
        "Selected pivot: arr[$index]=${arr[index]}"

    fun probe(index: Int, arr: List<Int>): String =
        "Probing index $index: value=${arr[index]}"

    fun found(index: Int, target: Int): String =
        "Found target $target at index $index!"

    fun notFound(target: Int): String =
        "Target $target not found in array"

    fun rangeCheck(low: Int, high: Int, mid: Int, arr: List<Int>, target: Int): String {
        val direction = if (arr[mid] > target) "left" else "right"
        return "Range [$low..$high], mid=$mid (val=${arr[mid]}), target=$target → go $direction"
    }
}
```

### Pseudocode Definitions (per algorithm)

**BubbleSort:**
```
1  function bubbleSort(arr):
2    for i = 0 to n-2:
3      for j = 0 to n-i-2:
4        if arr[j] > arr[j+1]:
5          swap(arr[j], arr[j+1])
6      if no swaps occurred:
7        break (already sorted)
```

**SelectionSort:**
```
1  function selectionSort(arr):
2    for i = 0 to n-2:
3      minIdx = i
4      for j = i+1 to n-1:
5        if arr[j] < arr[minIdx]:
6          minIdx = j
7      swap(arr[i], arr[minIdx])
```

**InsertionSort:**
```
1  function insertionSort(arr):
2    for i = 1 to n-1:
3      key = arr[i]
4      j = i - 1
5      while j >= 0 and arr[j] > key:
6        arr[j+1] = arr[j]
7        j = j - 1
8      arr[j+1] = key
```

**QuickSort:**
```
1  function quickSort(arr, lo, hi):
2    if lo < hi:
3      pivot = arr[hi]
4      i = lo - 1
5      for j = lo to hi-1:
6        if arr[j] <= pivot:
7          i = i + 1
8          swap(arr[i], arr[j])
9      swap(arr[i+1], arr[hi])
10     quickSort(arr, lo, i)
11     quickSort(arr, i+2, hi)
```

**MergeSort:**
```
1  function mergeSort(arr, l, r):
2    if l < r:
3      mid = (l + r) / 2
4      mergeSort(arr, l, mid)
5      mergeSort(arr, mid+1, r)
6      merge(arr, l, mid, r)
7  
8  function merge(arr, l, m, r):
9    copy left and right halves
10   while both halves have elements:
11     compare and copy smaller
12   copy remaining elements
```

**HeapSort:**
```
1  function heapSort(arr):
2    build max heap
3    for i = n-1 down to 1:
4      swap(arr[0], arr[i])
5      heapify(arr, 0, i)
6
7  function heapify(arr, i, n):
8    largest = i
9    left = 2i + 1
10   right = 2i + 2
11   if left < n and arr[left] > arr[largest]:
12     largest = left
13   if right < n and arr[right] > arr[largest]:
14     largest = right
15   if largest != i:
16     swap(arr[i], arr[largest])
17     heapify(arr, largest, n)
```

**ShellSort:**
```
1  function shellSort(arr):
2    gap = n / 2
3    while gap > 0:
4      for i = gap to n-1:
5        temp = arr[i]
6        j = i
7        while j >= gap and arr[j-gap] > temp:
8          arr[j] = arr[j-gap]
9          j = j - gap
10       arr[j] = temp
11     gap = gap / 2
```

**CountingSort:**
```
1  function countingSort(arr):
2    find max value k
3    create count[0..k] = 0
4    for each element x:
5      count[x]++
6    for i = 1 to k:
7      count[i] += count[i-1]
8    for i = n-1 down to 0:
9      output[count[arr[i]]-1] = arr[i]
10     count[arr[i]]--
11   return output
```

**CocktailSort:**
```
1  function cocktailSort(arr):
2    swapped = true
3    start = 0, end = n-1
4    while swapped:
5      swapped = false
6      for i = start to end-1:
7        if arr[i] > arr[i+1]:
8          swap(arr[i], arr[i+1])
9          swapped = true
10     if !swapped: break
11     end--
12     for i = end-1 down to start:
13       if arr[i] > arr[i+1]:
14         swap(arr[i], arr[i+1])
15         swapped = true
16     start++
```

**CycleSort:**
```
1  function cycleSort(arr):
2    for cycleStart = 0 to n-2:
3      item = arr[cycleStart]
4      pos = cycleStart
5      for i = cycleStart+1 to n-1:
6        if arr[i] < item:
7          pos++
8      if pos == cycleStart: continue
9      while item == arr[pos]:
10       pos++
11     swap(item, arr[pos])
12     while pos != cycleStart:
13       pos = cycleStart
14       for i = cycleStart+1 to n-1:
15         if arr[i] < item: pos++
16       while item == arr[pos]: pos++
17       swap(item, arr[pos])
```

**RadixSort:**
```
1  function radixSort(arr):
2    maxVal = max(arr)
3    exp = 1
4    while maxVal / exp > 0:
5      countingSort by digit (arr, exp)
6      exp *= 10
7
8  function countingSort(arr, exp):
9    create count[0..9] = 0
10   for each element:
11     digit = (element / exp) % 10
12     count[digit]++
13   for i = 1 to 9:
14     count[i] += count[i-1]
15   for i = n-1 down to 0:
16     digit = (arr[i] / exp) % 10
17     output[count[digit]-1] = arr[i]
18     count[digit]--
19   copy output to arr
```

**LinearSearch:**
```
1  function linearSearch(arr, target):
2    for i = 0 to n-1:
3      if arr[i] == target:
4        return i (found)
5    return -1 (not found)
```

**BinarySearch (Iterative):**
```
1  function binarySearch(arr, target):
2    left = 0, right = n-1
3    while left <= right:
4      mid = (left + right) / 2
5      if arr[mid] == target:
6        return mid (found)
7      else if arr[mid] < target:
8        left = mid + 1
9      else:
10       right = mid - 1
11   return -1 (not found)
```

**BinarySearch (Recursive):**
```
1  function binarySearch(arr, target, left, right):
2    if left > right:
3      return -1 (not found)
4    mid = (left + right) / 2
5    if arr[mid] == target:
6      return mid (found)
7    else if arr[mid] < target:
8      return search(arr, target, mid+1, right)
9    else:
10     return search(arr, target, left, mid-1)
```

### Example: BubbleSortVisualizer After Changes
```kotlin
class BubbleSortVisualizer : VisualizableAlgorithm {
    override suspend fun execute(input: List<Int>, emitter: MutableSharedFlow<AlgorithmEvent>) {
        val arr = input.toMutableList()
        emitter.emit(AlgorithmEvent.Start(input))

        for (i in 1 until arr.size) {
            var swapped = false
            for (j in 0 until arr.size - i) {
                emitter.emit(AlgorithmEvent.Compare(
                    indices = j to j + 1,
                    description = "Comparing arr[$j]=${arr[j]} with arr[${j+1}]=${arr[j+1]}",
                    pseudocodeLine = 4
                ))
                if (arr[j] > arr[j + 1]) {
                    arr.swapAt(j, j + 1)
                    emitter.emit(AlgorithmEvent.Swap(
                        indices = j to j + 1,
                        description = "Since ${arr[j+1]} > ${arr[j]}, swap positions $j and ${j+1}",
                        pseudocodeLine = 5
                    ))
                    swapped = true
                }
            }
            if (!swapped) {
                emitter.emit(AlgorithmEvent.Complete(
                    result = arr.toList(),
                    description = "No swaps in this pass — array is sorted!"
                ))
                return
            }
        }
        emitter.emit(AlgorithmEvent.Complete(arr.toList()))
    }
}
```

## Related Code Files

### Modify (14 algorithm files + registry)
| File | Changes |
|------|---------|
| `algo-core/.../sorts/BubbleSort.kt` | Add descriptions + pseudocodeLine to all events |
| `algo-core/.../sorts/SelectionSort.kt` | Same |
| `algo-core/.../sorts/InsertionSort.kt` | Same |
| `algo-core/.../sorts/QuickSort.kt` | Same |
| `algo-core/.../sorts/MergeSort.kt` | Same |
| `algo-core/.../sorts/HeapSort.kt` | Same |
| `algo-core/.../sorts/ShellSort.kt` | Same |
| `algo-core/.../sorts/CountingSort.kt` | Same |
| `algo-core/.../sorts/CocktailSort.kt` | Same |
| `algo-core/.../sorts/CycleSort.kt` | Same |
| `algo-core/.../sorts/RadixSort.kt` | Same |
| `algo-core/.../searches/LinearSearch.kt` | Same |
| `algo-core/.../searches/IterativeBinarySearch.kt` | Same |
| `algo-core/.../searches/RecursiveBinarySearch.kt` | Same |
| `algo-ui-shared/.../model/AlgorithmRegistry.kt` | Populate full metadata for all 14 entries |

### Create
| File | Purpose |
|------|---------|
| `algo-core/.../utils/DescriptionUtils.kt` | DRY description string formatting |

## Implementation Steps

1. **Create `DescriptionUtils.kt`** — Common description formatting functions
2. **Update BubbleSort.kt** — Pilot implementation, verify descriptions appear in UI later
3. **Update remaining 10 sort files** — Same pattern as BubbleSort
4. **Update 3 search files** — Search-specific descriptions (probe, range, found)
5. **Populate AlgorithmRegistry** — Add full metadata for all 14 entries including pseudocode definitions
6. **Run tests** — Verify existing tests still pass (default params)

## Todo
- [ ] Create DescriptionUtils.kt
- [ ] Update BubbleSort.kt with descriptions + pseudocode lines
- [ ] Update SelectionSort.kt
- [ ] Update InsertionSort.kt
- [ ] Update QuickSort.kt
- [ ] Update MergeSort.kt
- [ ] Update HeapSort.kt
- [ ] Update ShellSort.kt
- [ ] Update CountingSort.kt
- [ ] Update CocktailSort.kt
- [ ] Update CycleSort.kt
- [ ] Update RadixSort.kt
- [ ] Update LinearSearch.kt
- [ ] Update IterativeBinarySearch.kt
- [ ] Update RecursiveBinarySearch.kt
- [ ] Populate AlgorithmRegistry with rich metadata
- [ ] Run all tests

## Success Criteria
- [ ] Every Compare/Swap/Pivot/Overwrite/Probe/Found/RangeCheck event has non-empty description
- [ ] Every event has pseudocodeLine pointing to correct pseudocode line
- [ ] Descriptions reference actual array values at emission time
- [ ] All 14 algorithms have pseudocode definitions in AlgorithmRegistry
- [ ] All 14 algorithms have complexity, stability, difficulty, tags metadata
- [ ] All existing tests pass

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| Descriptions reference stale array values | Emit AFTER mutation, not before |
| Pseudocode line numbers don't match after edits | Keep pseudocode and line refs in sync manually |
| Lots of repetitive code across 14 files | DescriptionUtils.kt reduces duplication |
| CountingSort/RadixSort don't use Compare/Swap | Use Overwrite events with appropriate descriptions |

## Next Steps
- Phase 04 needs this + Phase 03 to integrate layout
