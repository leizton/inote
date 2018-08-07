并发集合分成阻塞式集合和非阻塞式集合

队列
Queue<E>
    // 入队
    put(E e)             阻塞; if 调用线程被interrupted, throw InterruptedException
    offer(E e):boolean   非阻塞; if 队列满, return false; if success, return true
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
// 无界队列
// 不能调用size()方法
- head:Node<E> volatile
- tail:Node<E> volatile
> ConcurrentLinkedQueue()
    $head = $tail = new Node  // 引入空节点
> Node
    - item:E volatile
    - next:Node<E> volatile
> offer(E e):boolean
    checkNotNull(e)
    newNode = new Node<E>(e)
    // $.tail无需始终指向尾节点
    p = t = $.tail;
    while 1
        if p.next == null && p.casNext(null, newNode)
            // cas成功前一刻p是实际尾节点, 成功后一刻newNode是实际尾节点
            // 所以cas成功就表示newNode入队成功
            if p != t
                casTail(t, newNode)  // tail不是实际尾节点，所以尝试更新tail
            return true
        q = p.next
        if p != q
            // p != t 条件使得 t = $.tail 执行次数降低, 减少volatile读取开销
            p = p != t && t != (t = $.tail) ? t : q  // 若tail有更新, 则取新的tail
        else
            p = t != (t = tail) ? t : head  // p == q, unknowState
> poll():E
    p = h = $.head
    while 1
        item = p.item
        if item != null && p.casItem(item, null)
            if p != h
                updateHead(h, p)
            return item
        q = p.next
        if q == null
            updateHead(h, p)
            return null
        else if p != q
            p = q
        else
            p = h = $.head  // p == q, unknowState

非阻塞并发双端队列
ConcurrentLinkedDeque<T> implements Deque<E>

阻塞队列
BlockingQueue<E> extends Queue<E>
> 新增接口
    offer(E e, long tm, TimeUnit tu):boolean
    poll(long tm, TimeUnit tu):E
    drainTo(Collection<? extends E> c)
    drainTo(Collection<? extends E> c, int maxNum)
> 实现子类
    SynchronousQueue
    ArrayBlockingQueue
    LinkedBlockingQueue

无缓冲的阻塞队列
SynchronousQueue
// put(e)会阻塞至有receiver来取
// offer(e)会立即返回, 若无receiver则丢弃
// Executors.newCachedThreadPool()里使用SynchronousQueue, 有新task时创建新thread

有缓冲的阻塞队列
ArrayBlockingQueue<E>
- lock                       ReentrantLock final
- putIndex, takeIndex, count int
- items                      Object[] final
> offer(E e):boolean
    $.lock.lock()
    try {
        if this.count == this.items.length
            return false
        else
            this.enqueue(e)
            return true
    } finally {
        $.lock.unlock()
    }
> enqueue(E e)
    $.items[$.putIndex] = e
    if ++$.putIndex == items.length
        $.putIndex = 0
    $.count++
    $.notEmpty.singal()  // 通知

转移队列 
TransferQueue<E>
> 接口
    tryTransfer(E e):boolean  // 尝试直接把e给consumer; 若没有consumer等待则返回false, 不入队列
    transfer(E e)  // 没有waiting consumer时block
    tryTransfer(E e, long tm, TimeUnit tu):boolean
    hasWaitingConsumer():boolean
    getWaitingConsumerCount():int
LinkedTransferQueue<E>

优先阻塞队列
PriorityBlockingQueue
// 按指定比较顺序出队

延迟无界阻塞队列
DelayQueue
// 基于PriorityQueue实现
// 使用场景: 缓存过期; 定时调度

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