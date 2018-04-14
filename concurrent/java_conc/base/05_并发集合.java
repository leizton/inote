并发集合分成阻塞式集合和非阻塞式集合

队列
Queue<E>
    // 入队
    put(E e)             阻塞; if 调用线程被interrupted, throw InterruptedException
    offer(E e):boolean   非阻塞; if 队列满, return false; if success, return true
    // add(E e):boolean  用offer替代; 非阻塞; if 队列满, throw IllegalStateException; if success, return true
    // 出队
    take():E             阻塞; if 调用线程被interrupted, throw InterruptedException
    poll():E             非阻塞; if 队列非空, 返回并删除队列的头元素; else 队列空, 返回null
    //
    peek():E             if 队列非空, 返回队列的头元素, 但不删除; else 队列空, 返回null
    // element():E       用peek替代; if 队列非空, 返回队列的头元素, 但不删除; else 队列空, throw NoSuchElementException

双端队列
Deque<E>
    // 首尾插入新节点
    addFirst/addLast(E e):void throw IllegalStateException
    offerFirst/offerLast(E e):boolean
    // 删除并获取首尾节点
    removeFirst/removeLast():E throw NoSuchElementException
    pollFirst/pollLast():E
    // 获取首尾节点
    getFirst/getLast():E throw NoSuchElementException
    peekFirst/peekLast():E

非阻塞并发队列
ConcurrentLinkedQueue<E> implements Queue<E>
// 不推荐使用该无锁队列
// 推荐使用SingleProducerMultiConsumer或MultiProducerSingleConsumer的无锁队列
- head:Node<E> volatile
- tail:Node<E> volatile
> ConcurrentLinkedQueue()
    $head = $tail = new Node
> Node
    - item:E volatile
    - next:Node<E> volatile
> offer(E e):boolean
    checkNotNull(e)
    newNode = new Node<E>(e)
    // $tail并不是总是指向尾节点
    t = $tail; p = t
    while 1
        q = p.next
        if q == null
            if p.casNext(null, newNode)
                // cas成功后的一瞬间newNode是实际尾节点, 因为这一瞬间前p.next是null, p是实际尾节点
                // 所以cas成功就表示newNode入队成功
                if p != t
                    casTail(t, newNode)  // tail距离实际尾节点较远，所以更新tail
                return true
        else if p != q
            if p == t
                p = q  // q!=null => p大概率下不是实际尾节点 => p往后移动
            else if t != (t = $tail)
                p = t  // $tail有更新, p取新的$tail
            else
                p = q
        else
            // p == q
            p = (t != (t = tail)) ? t : head

非阻塞并发双端队列
ConcurrentLinkedDeque<T> implements Deque<E>

阻塞队列
BlockingQueue<E> extends Queue<E>
> 接口
    offer(E e):boolean
    offer(E e, long t, TimeUnit tu):boolean
> 实现子类
    ArrayBlockingQueue
    LinkedBlockingQueue
> ArrayBlockingQueue::offer(E e):boolean
    final ReentrantLock lock = this.lock
    lock.lock()
    try {
        if this.count == this.items.length
            return false
        else
            this.enqueue(e)
            return true
    } finally {
        lock.unlock()
    }
> ArrayBlockingQueue::enqueue(E e)
    items[putIndex] = e
    if ++putIndex == items.length
        putIndex = 0
    count++
    notEmpty.singal()  // 通知

有序Map
SortedMap<K,V> extends Map<K,V>
    // key有序的map
    subMap(K fromKey, K toKey):SortedMap<>  结果包含fromKey和toKey
        return this.headMap(toKey).tailMap(fromKey)
    tailMap(K fromKey):SortedMap<>
    headMap(K toKey):SortedMap<>

有序Map的升级
NavigableMap<K,V> extends SortedMap<K,V>
    firstEntry():Map.Entry<K,V>  返回key最小的kv-pair
    lastEntry():Entry<>
    lowerEntry(K key):Entry<>    返回比key小的kv-pair里最大的pair
    subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive)

有序Map的实现
TreeMap<K,V> implements NavigableMap<K,V>

并发有序Map
ConcurrentNavigableMap extends ConcurrentMap, NavigableMap

并发有序Map的实现
ConcurrentSkipListMap implements ConcurrentNavigableMap