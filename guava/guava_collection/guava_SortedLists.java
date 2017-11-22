SortedLists
	实现升序list上的二分查找
内部枚举
	enum KeyPresentBehavior {
		// 当key存在时, list中可能有多个元素与key相同.
		abstract <E> resultIndex(Comparator<? super E> comparator, E key, List<? extends E> list, int foundIndex):int;
		// resultIndex的各种实现
		LAST_PRESNET {  // 最后一个位置
			@Override resultIndex() {
				lower = foundIndex  // foundIndex后面的元素 >= key
				upper = list.size() - 1
				while lower < upper
					middle = (lower + upper) >>> 1
					comp = comparator.compare(key, list.get(middle))
					if comp > 0
						upper = middle - 1
					else  // comp == 0
						lower = middle
				return lower
			}
		},
		FIRST_AFTER { return LAST_PRESNET.resultIndex() + 1 },
		FIRST_PRESENT,  // 第一个位置
		LAST_BEFORE { return FIRST_PRESENT.resultIndex() - 1 },
		ANY_PRESENT { return foundIndex };  // 任意一个位置
	}
静态方法
	// list已是升序
	<E> binarySearch(List<? extends E> list, E key, Comparator<? super E> comparator,
			KeyPresentBehavior present, KeyAbsentBehavior absent) {
		if ! list instanceof RandomAccess  // java.util.RandomAccess是一个空接口
			list = Lists.newArrayList(list)
		int lower = 0
		int upper = list.size() - 1
		while lower <= upper
			int middle = (lower + upper) >>> 1
			int comp = comparator.compare(key, list.get(middle))
			if comp < 0  // key < list[middle]
				upper = middle - 1
			else if comp > 0
				lower = middle + 1
			else
				return lower + present.resultIndex(
					comparator, key, list.subList(lower, upper+1), middle-lower);
		return absent.resultIndex(lower)
	}
	<E> binarySearch(List<? extends E> list, E key,	KeyPresentBehavior present, KeyAbsentBehavior absent) {
		return binarySearch(list, key, Ordering.natural, present, absent)
	}