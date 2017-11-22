/* byte
 * if(size > 29) 计数排序, else 插入排序.
 */
public static void sort(byte[] a)
public static void sort(byte[] a, int fromIndex, int toIndex)

/* int long float double
 * if(size < 47) 插入排序,
 * elif(size < 286 || 数组不是相对有序) 快排,
 * else 归并排序.
 */
public static void sort(int[] a)

// Object
public static void sort(Object[] a) {
	if LegacyMergeSort.userRequested
		legacyMergeSort(a)  // 遗留下来的以前的归并排序, jdk1.7之后弃用原来的归并排序
	else
		/* TimSort 是Tim Peters于2002年用在Python中的归并插入混合排序.
		 * 最小归并单元从数组元素变成一个分区, 分区称作"run".
		 * 第一步, 把待排数组划分成一个个run, 长度小于阈值时用插入排序扩充;
		 * 第二步, 将run入栈，当栈顶的3个run的长度不满足约束条件时, 归并3个中最短的2个run成一个, 直至栈空.
		 * 归并约束条件使得归并时的两个数组长度接近.
		 */
		ComparableTimSort.sort(a, 0, a.length, null, 0, 0)
}