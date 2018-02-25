// n 的 1/7
int seventh = (n >> 3) + (n >> 6) + 1;  // n * 9/64 + 1

// 二分查找
int ret = Arrays.binarySearch(arr, target);
if (ret < 0) {
    // 设pos是target插入的位置, pos>=0, 则ret是(-pos-1)
	// pos求反加1是-pos, 则ret=(-pos-1)是pos求反
	// 所以~ret是post
    ret = ~ret;
}
