Ordering<T>
	implements Comparator<T>
方法
	// 以下4个方法实现TopK
	greatestOf(Iterable<E> iterable, int k):List<E> <E extends T> {
		return reverse().leastOf(iterable, k)
	}
	greatestOf(Iterator<E> iterator, int k):List<E> <E extends T> {
		return reverse().leastOf(iterator, k)
	}
	leastOf(Iterable<E> iterable, int k):List<E> <E extends T> {
		if iterable instanceOf Collection
			Collection c = (Collection) iterable
			if c.size() <= 2 * k
				E[] array = (E[]) c.toArray()
				Arrays.sort(array, this)  // Ordering实现了Comparator接口
				if array.length > k
					array = ObjectArrays.arraysCopyOf(array, k)
				return Collections.unmodifiableList(Arrays.asList(array))
		return leastOf(iterable.iterator(), k)  // 调用下面的leastOf()
	}
	leastOf(Iterator<E> elements, int k):List<E> <E extends T> {
		if k >= Integer.MAX_VALUE / 2
			ArrayList<E> list = Lists.newArrayList(elements)
			Collections.sort(list, this)
			if list.size() > k
				list.subList(k, list.size()).clear()  // 清除索引-k后面的元素
			list.trimToSize()
			return Collections.unmodifiableList(list)
		
		/* 实现平均复杂度是O(n + klogk)的TopK算法.
		 * 用到了在无序数组中查找大小是第k个元素的算法, 平均时间复杂度O(n),
		 *    最坏时间O(n^2), 参考《算法导论》第9章.
		 */
		 final int bufCap = k * 2
		 E[] buf = (E[]) new Object[bufCap]
		 buf[0] = elements.next()
		 int bufSize = 1
		 E threshold = buf[0]
		 // 先填入k个元素
		 while bufSize < k && elements.hasNext()
		 	E e = elements.next()
		 	buf[bufSize++] = e
			threshold = max(threshold, e)
		// 遍历剩余 n-k 个元素
		while elements.hasNext()
			E e = elements.next
			if compare(e, threshold) >= 0  // e >= threshold
				continue
			buf[bufSize++] = e
			if bufSize == bufCap  // 放满了 k*2 个元素
				int left = 0, right = bufCap - 1
				minThresholdPosition = 0
				while left < right
					int pivotIndex = (left + right + 1) >>> 1
					// partition()的时间复杂度是O(n)
					int pivotNewIndex = partition(buf, left, right, pivotIndex)
					if pivotIndex > k
						right = pivotIndex - 1
					else if pivotIndex < k
						left = Math.max(pivotNewIndex, left+1)  // pivotNewIndex可能就是left
						/* minThresholdPosition: 阈值位置的最小值
						 * 新的阈值元素出现在minThresholdPosition的右边
						 */
						minThresholdPosition = pivotNewIndex
					else
						break
				bufSize = k
				// 找到新的阈值
				threshold = buf[minThresholdPosition]
				for i = minThresholdPosition + 1; i < k; i++
					threshold = max(threshold, buf[i])
		// end while elements.hasNext()
		Arrays.sort(buf, 0, bufSize, this)  // 这里排序占用O(klogk), Ordering实现了Comparator接口
		bufSize = Math.min(bufSize, k)
		return Collections.unmodifiableList(
			Arrays.asList( ObjectArrays.arraysCopyOf(buf, bufSize) ) )
	}
	// 快排的Partition
	partition(E[] values, int left, int right, int pivotIndex):int {
		E pivotValue = values[pivotIndex]
		ObjectArrays.swap(values, pivotIndex, right)  // 交换数组的 第pivotIndex 和 第right 这两个元素
		
		int storeIndex = left
		for i = left; i < right; i++
			if compare(values[i], pivotValue) < 0  // values[i] < pivotValue
				// [left, storeIndex)区间上的元素比pivotValue小
				// [storeIndex, right)区间上的元素比pivotValue大
				// 此时values[right]是pivotValue
				ObjectArrays.swap(values, i, storeIndex)
				storeIndex++
		ObjectArrays.swap(values, right, storeIndex)
		return storeIndex
	}
	// 自然顺序
	static <C extends Comparable> natural():Ordering<C> {
		return (Ordering<C>) NaturalOrdering.INSTANCE
	}


NaturalOrdering
	extends Ordering<Comparable>
字段
	static NaturalOrdering INSTANCE = new NaturalOrdering()
方法
	@Override compare(Comparable l, Comparable r):int {
		checkNotNull: l, r
		return l.compareTo(r)
	}
	@Override <S extends Comparable> reverse():Ordering<S> {
		return (Ordering<S>) ReverseNaturalOrdering.INSTANCE
	}


ReverseNaturalOrdering
	extends Ordering<Comparable>
字段
	static ReverseNaturalOrdering INSTANCE = new ReverseNaturalOrdering()
方法
	@Override compare(Comparable l, Comparable r):int {
		checkNotNull: l
		if l == r
			return 0
		return r.compareTo(l)
	}
	@Override <S extends Comparable> reverse():Ordering<S> {
		return Ordering.natural()
	}