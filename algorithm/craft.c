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

// 判断是2的幂次数
bool isPowOf2(int n) {
    return n > 0 && (n&(n-1)) == 0;
}

// 返回比n大的最小的2的幂次数
int alignToPowOf2(int n) {
    if (n <= 1) {
        return 1;
    }
    if (n > (1<<29)) {
        return (1<<30);
    }
    --n;  // 设n是(x1,x2,x3), 减1后是(1,y1,y2)或(0,1,1)
    // 把n的最高位1右边的bit都变成1
    n |= (n>>1);  // 使得n的最高位和次高位都是1
    n |= (n>>2);  // 使得n的最高4位都是1
    n |= (n>>4);
    n |= (n>>8);
    n |= (n>>16);
    return n+1;
}