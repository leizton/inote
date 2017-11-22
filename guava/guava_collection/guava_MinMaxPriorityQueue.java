最小最大堆
支持操作:
	1. 插入任意值到集合中
	2. 删除集合中的最小值
	3. 删除集合中的最大值
例子:
0                 1
             /        \
1           10         7
          /    \     /   \
2       2       4   3     5
       / \     /
3     8   9   6
    是完全二叉树, 可用数组queue[]存储.
	queue[0]是最小值, queue[1/2]是最大值, queue[size-1]是中位数.
性质:
	1. 树深度是偶数的节点, 值比父节点(在奇数层)小, 比祖父节点(在偶数层)大;
	2. 树深度是奇数的节点, 值比父节点(在偶数层)大, 比祖父节点(在奇数层)小;
推论:
	1. 对于任意节点, 值位于父节点和祖父节点之间;
	2. 单看偶数层, 从上往下节点值递增; 单看奇数层, 从上往下节点值递减;
	   即偶数层是最小堆, 奇数层是最大堆.


MinMaxPriorityQueue
内部类
	Heap
	字段
		@Weak Heap otherHeap
		Ordering<E> ordering  // 最小堆和最大堆的ordering.compare()返回符号正好相反
	方法
		getLeftChildIndex  (int i)  { return i*2 + 1   }
		getRightChildIndex (int i)  { return i*2 + 2   }
		getParentIndex     (int i)  { return (i-1) / 2 }
		getGrandparentIndex(int i)  { return (i-3) / 4 }  // ((i-1)/2 - 1) / 2 == (i-3)/4
		// 向上调整的过程分两步: 先保证父节点条件(可能换到另一个堆中), 再向上调整保证祖父节点条件.
		bubbleUp(int index, E e) {
			// 先和父节点(在另外一个堆中)调整
			int crossOver = crossOverUp(index, e)
			Heap heap = (crossOver == index) ? this : otherHeap
			// 再在同一个堆中调整
			heap.bubbleUpAlternatingLevels(crossOver, e)
		}
		// 完成向上调整的第一步: 满足父节点条件.
		crossOverUp(int index, E e) {
			if index == 0
				queue[0] = e
				return 0
			int parentI = getParentIndex(index)  // 父节点的索引
			E parentE = queue[parentI]  // 外部类的字段queue
			if parentI != 0
				int grandparentI = getParentIndex(parentI)
				int uncleI = getRightChildIndex(grandparentI)  // grandparentI右子节点的索引
				if uncleI != parentI  // parentI是grandparentI的左子节点, uncleI是右子节点
				   && getLeftChildIndex(uncleI) >= size  // uncleI没有子节点
					E uncleE = queue[uncleI]
					if ordering.compare(uncleE, parentE) < 0
						/* 对于最小堆, 选择更小的节点与新节点交换.
						 * 目的是让queue[size-1]是中位数. */
						parentI = uncleI
						parentE = uncleE
			if ordering.compare(e, parentE) > 0
				/* 对于最小堆, ordering.compare()是正向,
				 * e > parentE, 不符合偶数层节点比父节点小条件. */
				// 交换index和parentI, 新节点换到另一个堆中
				queue[index] = parentE
				queue[parentI] = e
				/* parent移到子节点是成立的.
				 * 例如, 当parent在最大堆上, 现移到下一层的最小堆上.
				 * 1. parent以前大于父节点, 则现在大于祖父节点, 符合值比祖父节点(在最小堆)大条件;
				 * 2. parent小于e, 符合值比父节点(e和parent交换了)小条件.
				 *      假设接下来e只在同一个堆中调整, 即e只在最大堆中调整,
				 *      e若调整了, 则e比祖父节点大, 此时e的祖父节点是parent以前的祖父节点,
				 *      parent以前小于祖父节点, 因此e的调整不影响parent.
				 */
				/* 接下来证明只要在同一个堆中调整e. 假设e在最大堆中,
				 * e与其祖父节点交换或不交换, e处的节点比其父节点大.
				 *   因为e处的父节点是在最小堆中, 则e处的父节点比自身的父节点(e处的祖父节点)小,
				 *      e和parent交换前, parent在最大堆中, 有e > parent > e处父节点.
				 *   所以e在同一个堆中调整或不调整不会使另一个堆的节点不满足条件.
				 */
				/* 本质上证明的是堆中调整不会使另一个堆的节点不满足条件.
				 *   设 x-a-y-b-z, 排在前面的是后面的父节点, xyz在最小堆, ab在最大堆.
				 *   则有 a > b > z > y > x, 因为a>b和z>y>x分别是最大最小堆性质, b>z是最小堆的节点比父节点小.
				 *   所以a和b的交换不会影响xyz.
				 */
				return parentI  // 返回交换后索引
			queue[index] = e
			return index
		}
		// 把index处节点向上调整, 只在本堆里调整, 返回最后的调整结果
		bubbleUpAlternatingLevels(int index, E e):int {
			while index > 2  // 保证index有祖父节点
				int grandparentI = getGrandparentIndex(index)
				E grandparentE = queue[grandparentI]
				if ordering.compare(e, grandparentE) > 0
					/* 对于最小堆, e > grandparentE,
					 * 符合偶数层节点比祖父节点大条件 */
					return index
				queue[index] = grandparentE
				index = grandparentI
			return index
		}
		// 空洞位置在同一个堆中往下移动, 返回最终空洞位置
		fillHoleAt(int index):int {
			/* 以最小堆作例子. 由于孙节点小于他(孙节点)的父节点(最大堆上),
			 * 选择最小孙节点符合所以孙节点小于本节点的祖父节点条件,
			 * 由于孙节点是小于孙节点的父节点, 所以最小的孙节点小于所有孙节点的父节点, 符合父节点条件. */
			while minGrandchildI = findMinGrandChild(index), minGrandchildI > 0
				queue[index] = queue[minGrandchildI]
				index = minGrandchildI
		}
		/* 孙节点中值最小的节点的索引.
		 * 对于最小堆是最小, 对于最大堆是最大. */
		findMinGrandChild(int index) {
			int leftChildI = getLeftChildIndex(index)
			return (leftChildI < 0) ? -1 :
					findMin(getLeftChildIndex(leftChildI), 4)  // 数组中的4个元素
		}
		/* 先判断是否满足父节点条件(所以函数名称有try),
		 * 满足则不做祖父节点调整, 不满足再向上调整保证在另一个堆中的祖父节点条件. */
		tryCrossOverAndBubbleUp(int removeIndex, int vacated, E toTrickle) {
			int crossOver = crossOver(vacated, toTrickle)  // 保证父节点条件
			if crossOver == vacated  // 父节点条件本来就满足, 因为已经经过祖父节点调整.
				return null
			E parent = crossOver < vacated ? queue[removeIndex] : queue[getParentIndex(removeIndex)]
			
		}
	MoveDesc<E>
		/* removeAt的返回类型.
		 * 全称: move description, 移动描述.
		 * 表示移动后是 replaced这个key 替换了 toTrickle这个key. */
		字段: final E toTrickle, replaced. 存放两个引用.
字段
	- Object[] queue  // 用数组存放完全二叉树(最小最大堆)
	- int size  // queue的有效元素个数
	- int maximumSize  // queue的最大容量, 即queue.length的最大值
	- final Heap minHeap  // 最小堆
	- final Heap maxHeap  // 最大堆
静态方法
	isEvenLevel(int i) {
		oneBase = i + 1  // oneBase从1开始
		return (oneBase & 0x55555555) > (oneBase & 0xaaaaaaaa);
	}
方法
	offer(E e):boolean {
		// 新节点先插入最后一个位置, 等待调整
		insertIndex = size++
		/* if size > queue.length, 扩容.
		   if queue.length < 64, 扩大成原来的2倍;
		   else 扩大成原来的1.5倍 */
		grewIfNeeded()
		// 判断最后一个位置是最大堆还是最小堆
		Heap heap = heapForIndex(insertIndex)
		// 新节点从下往上调整
		heap.bubbleUp(insertIndex, e)
		// if size > maximumSize, 调用pollLast()删除最后一个元素
		return size <= maximumSize || pollLast() != e
	}
	heapForIndex(int i):Heap {
		return isEvenLevel(i) ? minHeap : maxHeap
	}
	poll():E { return isEmpty() ? null : removeAndGet(0) }  // 删除最小值节点
	pollFirst():E { return poll() }  // 删除最小值节点
	pollLast():E { return isEmpty() ? null : removeAndGet(getMaxElementIndex()) }  // 删除最大值节点
	getMaxElementIndex():int {  // 最大值节点的索引
		switch size
			case 1: return 0
			case 2: return 1
			default: return (maxHeap.compareElements(1,2) <= 0) ? 1 : 2
	}
	removeAndGet(int index):E {
		E e = queue[index]
		removeAt(index)
		return e
	}
	removeAt(int index):MoveDesc<E> {
		checkPositionIndex(index, size)
		size--
		if size == index  // 移除数组最后一个元素
			queue[size] = null
			return null
		
		E lastE = queue[size]
		/* getCorrectLastElement可能返回lastE的uncle节点
		 * 目的是让queue[size-1]是中位数. */
		int lastI = heapForIndex(size).getCorrectLastElement(lastE)
		
		// 用最后一个元素替换index处元素, 直接删除最后一个元素
		E toTrickle = queue[size]
		queue[size] = null
		MoveDesc<E> changes = fillHole(index, toTrickle)  // 用最后一个元素替补index处元素
		
		return lastI < index
			? (changes == null)
				? new MoveDesc<E>(lastE, toTrickle)
				: new MoveDesc<E>(lastE, changes.replaced)
			: changes
	}
	fillHole(int index, E toTrickle):MoveDesc<E> {
		Heap heap = heapForIndex(index)
		int vacated = heap.fillHoleAt(index)  // 填补空洞, 空洞位置在同一个堆中往下移动, 返回值是最终空洞位置.
		
		int bubbledTo = heap.bubbleUpAlternatingLevels(vacated, toTrickle)
		if bubbledTo == vacated
			/* 没有向上调整. toTrickle满足祖父节点条件.
			 * 先判断是否满足父节点条件, 满足则不进行bubbleUp, 否则进行bubbleUp. */
			return heap.tryCrossOverAndBubbleUp(index, vacated, toTrickle)
		else
			/* 有向上调整.
			 * 假设调整前toTrickle在最小堆上, 则toTrickle小于父节点.
			 *   toTrickle可以往上调整说明他小于祖父节点, 而他的父节点大于他的祖父节点所以他小于父节点, 往上调后符合条件.
			 *   toTrickle的祖父节点调下来是回到fillHoleAt()前位置, 所以符合条件.
			 * toTrickle满足父节点条件. */
			return (bubbledTo < index) ? new MoveDesc<E>(toTrickle, queue[index]) : null
	}