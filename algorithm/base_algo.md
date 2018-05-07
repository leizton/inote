# 最长公共子串

# 最长不重复子序列

# 中位数、百分位数和topk

# 归并插入混合排序
- 把待排数组划分成多个run, 长度小于阈值时用插入排序扩充
  将run入栈, 当栈顶的3个run的长度不满足约束条件时, 归并3个中最短的2个run成一个, 直至栈空
- 归并约束条件使得归并时的两个数组长度接近
- java.util.ComparableTimSort

# topK
1. PriorityQueue(无界队列)
2. Guava 有界双端队列 MinMaxPriorityQueue
3. Guava Ordering
  order = new Ordering<T>() {
    public int compare(T left, T right) {
      return -1;
    }
  }
  order.greatestOf(iterable/iterator, k)