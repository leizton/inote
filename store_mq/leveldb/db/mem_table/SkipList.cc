kMaxHeight = 12


class SkipList<Key, Comparator>
> (Comparator cmp, Arena* arena)
    compare_(cmp)  // compare_比较时只比较前面的key
    arena_(arena)  // 一个内存池
    head_(this.NewNode(0, kMaxHeight))
    max_height_(reinterpret_cast<void*>(1)):AtomicPointer  // skiplist的高度, 实际是整数值的指针, 可用atomic<int>
    rand_:Random
> NewNode(Key& key, int height)
    char* p = arena_->AllocateAligned(sizeof(Node) + sizeof(AtomicPointer) * (height-1))
    return new(p) Node(key)
> RandomHeight()
    static uint kBranching = 4
    int height = 1
    while height < kMaxHeight && rand_.Next(kBranching) == 0
        ++height
    return height
> Insert(Key& key)
    // key是一个char*, 包含实际的key和value, compare_比较时只比较实际的key
    Node* prev[kMaxHeight]
    FindGreaterOrEqual(key, prev)
    int height = this.RandomHeight
    if height > max_height_
        for i = max_height_:height
            prev[i] = head_
        max_height_.NoBarrier_Store(reinterpret_cast<void*>(height))
    // 插入new_node
    Node* new_node = NewNode(key, height)
    for i = 0:height
        new_node.NoBarrier_SetNext(i, prev[i].NoBarrier_Next(i))
        prev[i].SetNext(i, new_node)
> FindGreaterOrEqual(Key& key, Node** prev):Node*
    Node* p = head_
    int level = max_height_ - 1  // 从最高层开始查找
    while true
        Node* next = p.Next(level)
        if KeyIsAfterNode(key, next)
            p = next  // key更大, 则直接跳到next, 在当前这个链表里继续查找
        else
            // next==NULL, 或key位于x和next之间, 则往下一层链表查找
            if prev != NULL, prev[level] = p
            if level == 0, return next  // next可能是NULL, 此时key比最大的还大
            --level
> KeyIsAfterNode(Key& key, Node* n)  // key比n.key大
    return n != NULL && key > n.key


class Node
> (Key& k)
    key(k)  // 垂直方向上的key是同一个
    next_[1]:AtomicPointer  // 占位数组, 实际长度在NewNode()时确定
> Next(int h):Node* = next_[h].Acquire_Load()
> SetNext(int h, Node* x) = next_[h].Release_Store(x)
> NoBarrier_Next(int h):Node* = next_[h].NoBarrier_Load()
> NoBarrier_SetNext(int h, Node* x) = next_[h].NoBarrier_Store(x)


class Iterator
> (SkipList* list)
    list_(list)
    node_:Node*
> Valid()
    return node != NULL
> Seek(Key& target)
    node_ = list_.FindGreaterOrEqual(target, NULL)