计算topK的方法

1. PriorityQueue(无界队列)

2. Guava 有界双端队列 MinMaxPriorityQueue

3. Guava Ordering
	Ordering<T> order = new Ordering<T>() {
		@Override
		public int compare(T left, T right) {
			return -1;
		}
	};
	order.greatestOf(iterable/iterator, topK);