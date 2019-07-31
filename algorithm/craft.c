// n 的 1/7
int seventh = (n >> 3) + (n >> 6) + 1;  // n * 9/64 + 1

// 二分查找
int ret = Arrays.binarySearch(arr, target);
if (ret < 0) {
    // 设pos是target插入的位置, pos>=0, 则ret是(-pos-1)
	// pos求反加1是-pos, 则pos求反是(-pos-1)即ret, 所以~ret是pos
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

// 统计数组的逆序对数目, 排序所需的最小交换次数
// 分治思路, 归并排序
template<typename T>
int numOfInvertedPairs(T a[], int begin, int end) {
    if (a == NULL || begin < 0 || end <= begin+1) {
        return 0;
    }

    T* copy = new T[(end-begin) * 2];
    std::memcpy(copy, a, (end-begin) * sizeof(T));
    std::memcpy(copy+(end-begin), a, (end-begin) * sizeof(T));
    int num = numOfInvertedPairsImpl(copy+(end-begin), copy, 0, end-begin);
    delete[] copy;
    return num;
}
template<typename T>
int numOfInvertedPairsImpl(T src[], T copy[], int begin, int end) {
    if (begin+1 == end) {
        return 0;
    }

    // 把copy左右两边的排序结果分别放到src的左右边
    // src的[begin,end)被打乱, 递归的上一层不受影响
    // 例如, 当前层的下一层会分别打乱copy的[begin,mid)和[mid,end), 但不影响排序结果merge到copy
    int mid = (begin + end) >> 1;
    int num = numOfInvertedPairsImpl(copy, src, begin, mid);
    num += numOfInvertedPairsImpl(copy, src, mid, end);

    // 把src的左右merge到copy, 最终排序结果在copy上
    int i = begin, j = mid, k = begin;
    while (i < mid && j < end) {
        if (src[i] <= src[j]) {
            copy[k++] = src[i++];
        } else {
            copy[k++] = src[j++];
            num += mid - i;
        }
    }
    while (i < mid) {
        copy[k++] = src[i++];
    }
    while (j < end) {
        copy[k++] = src[j++];
    }
    return num;
}

// 打乱数组
template<typename T>
void shuffle(T a[], int begin, int end) {
    if (a == NULL || begin < 0 || end <= begin) {
        return;
    }

    static std::random_device rndDev;
    static std::mt19937 rndGen(rndDev());
    int j;

    for (int i = begin + 1; i < end; ++i) {
        // 只跟自己前面的元素交换
        // 归纳法: 假设a[begin,i)已经shuffle好了, 现在新增a[i], 就是与前面一个元素随机交换
        std::uniform_int_distribution<int> rnd(begin, i + 1);
        j = rnd(rndGen);
        if (j != i) {
            std::swap(a[i], a[j]);
        }
    }
}
int main() {
    const int N = 5, TEST = 100000;
    const double SUM_TARGET = N * (N-1) / 4.0;
    int a[N];
    double sum;
    for (int k = 0; k < TEST; ++k) {
        for (int i = 0; i < N; ++i) a[i] = i+1;
        shuffle<int>(a, 0, N);
        sum += numOfInvertedPairs<int>(a, 0, N);
    }
    assert(abs(sum/TEST - SUM_TARGET) < 0.01);
    return 0;
}