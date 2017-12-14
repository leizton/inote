ReentrantLock
// example
- sync  Sync
> ReentrantLock()
    sync = new NonfairSync()
> lock()
    sync.acquire(1)
> unlock()
    sync.release(1)
> tryLock():boolean
    return sync.nonfairTryAcquire(1)
> tryLock(long timeout, TimeUnit unit):boolean throws InterruptedException
    return tryAcquireNanos(1, unit.toNanos(timeout))
> newCondition():Condition
    return sync.newCondition()

ReentrantLock::Sync extends AbstractQueuedSynchronizer
> nonfairTryAcquire(int acquires):boolean  // 返回是否成功占锁
    // 如果没有线程占锁, 就尝试占锁, 尝试成功返回true, 否则返回false
    // 如果锁已被其他线程占, 返回false
    // 锁被自己占有, 对于可重入锁, 在state上加acquires, 表示再次加锁
    curr = Thread.currentThread()
    s = getState()
    if s == 0
        if compareAndSetState(0, acquires)  // 尝试占锁
            setExclusiveOwnerThread(curr)   // AbstractOwnableSynchronizer
            return true
        return false
    if curr != getExclusiveOwnerThread()
        return false
    setState(s + acquires)  // assert c + acquires > 0
    return true
> tryRelease(int releases):boolean  // 返回是否完全释放了锁
    if Thread.currentThread() != getExclusiveOwnerThread()
        throw IllegalMonitorStateException
    nextState = getState() - releases  // assert nextState >= 0
    if nextState == 0
        setExclusiveOwnerThread(null)
    setState(nextState)
    return nextState == 0
> lock() abstract
> newCondition():ConditionObject
    return new ConditionObject()

ReentrantLock::NonfairSync extends Sync
> tryAcquire(int acquires):boolean
    return nonfairTryAcquire(acquires)


AbstractOwnableSynchronizer
// 用于跟踪当前是哪个线程独占了同步器
- exclusiveOwnerThread  Thread  当前独占同步器的线程
> setExclusiveOwnerThread(Thread)
> getExclusiveOwnerThread():Thread


AbstractQueuedSynchronizer
// 实现同步阻塞锁和如信号量等同步器的框架
- head   Node  只在enq()和setHead()里被修改
- tail   Node
- state  int
> addWaiter(Node mode):Node
    node = new Node(mode);
    enq(node)
    return node
> enq(Node node):Node  // 无锁式入队尾
    while true
        t = tail
        if t == null
            if compareAndSetHead(null, new Node())  // 初始化空的头结点
                tail = head
        else
            node.prev = t
            if compareAndSetTail(t, node)
                t.next = node
                return t
> setHead(Node node)
    head = node
    node.thread = node.prev = null  // 头结点是空结点
// 是否需要park
> shouldParkAfterFailedAcquire(Node prev, Node node):boolean
    // 返回当前线程是否应该休眠(park)
    int st = prev.waitStatus
    if st == Node.SIGNAL
        return true
    if st > 0
        // prev已经cancelled
        while prev.isCancelled
            node.prev = prev = prev.prev
        prev.next = node
    else
        // st == 0 or PROPAGATE
        prev.compareAndSetWaitStatus(st, Node.SIGNAL)
    return false
// 占锁
> acquire(int arg)
    // tryAcquire()由子类实现, 子类通过对state的cas修改实现
    if tryAcquire(arg)
        return  // tryAcquire成功时, 不会入队
    waiter = addWaiter(Node.EXCLUSIVE)
    if acquireQueued(waiter, arg)
        Thread.currentThread().interrupt()
> acquireQueued(Node node, int arg):boolean
    isInterrupted = false
    while true
        p = node.prev
        if p == head && tryAcquire(arg)  // 如果占锁且未入队的线程释放了锁, 这tryAcquire会成功
            setHead(node)
            p.next = null  // help gc
            return isInterrupted
        if shouldParkAfterFailedAcquire(p, node)
            LockSupport.park(this)  // 阻塞直到被unpark
            isInterrupted |= Thread.interrupted()
// 释放锁
> release(int arg):boolean
    if tryRelease(arg)  // tryRelease()由子类实现
        // 完全释放了锁, state==0
        h = head
        // 如果h.waitStatus==0, h.next不会进入park, 在acquireQueued里继续tryAcquire
        if h != null && h.waitStatus != 0
            unparkSuccessor(h)  // unpark h的后继结点(maybe h.next or h.next.next ...)
        return true
    return false
> unparkSuccessor(Node node)
    st = node.waitStatus
    if st < 0
        node.compareAndSetWaitStatus(st, 0)  // 表示node.next无需park
    nx = node.next
    if nx == null || nx.isCancelled
        nx = null  // 无需关心这个后继结点
        for t = tail;  t != null && t != node;  t = t.prev
            if t.waitStatus <= 0
                nx = t  // t未cancelled
    if nx != null
        LockSupport.unpark(nx.thread)
// 超时占锁
> tryAcquireNanos(int arg, long nanosTimeout):boolean throws InterruptedException
    if tryAcquire(arg)    return true   // 对于超时的实现, 应该至少尝试一次
    if nanosTimeout <= 0  return false  // check argument
    deadline = System.nanoTime() + nanosTimeout
    node = addWaiter(Node.EXCLUSIVE)
    while true
        p = node.prev
        if p == head && tryAcquire(arg)
            setHead(node)
            p.next = null
            return true
        leave = deadline - System.nanoTime()
        if leave <= 0
            cancelAcquire(node)
            return false
        if shouldParkAfterFailedAcquire(p, node) && leave > 1000  // 1ms
            LockSupport.parkNanos(this, leave)
        if Thread.interrupted()
            cancelAcquire(node)
            throw InterruptedException
//
> cancelAcquire(Node node)
    node.thread = null
    prev = node.prev
    while prev.isCancelled
        node.prev = prev = prev.prev
    prevNext = prev.next
    node.waitStatus = Node.CANCELLED
    if node == tail && compareAndSetTail(node, prev)
        // prev变成了tail
        prev.compareAndSetNext(prevNext, null)  // 如果prev继续是tail, 则cas会成功
        return
    if prev != head &&
       (st = prev.waitStatus) == Node.SIGNAL || (st <= 0 && prev.compareAndSetWaitStatus(st, Node.SIGNAL)) &&
       prev.thread == null
        // prev是一个有效的等待结点
        next = node.next
        if next != null && !next.isCancelled
            prev.compareAndSetNext(prevNext, next)
    else
        unparkSuccessor(node)

AbstractQueuedSynchronizer::Node
// 占锁mode
- SHARED      Node    static final  = new Node();
- EXCLUSIVE   Node    static final  = null;
// waitStatus取值
- CANCELLED   int     = 1
- SIGNAL      int     = -1      通知下一个结点可以进入休眠等待
- CONDITION   int     = -2
- PROPAGATE   int     = -3
//
- waitStatus  int     volatile  取值0表示next结点无需park, 直接tryAcquire
- prev        Node    volatile
- next        Node    volatile
- nextWaiter  Node    final
- thread      Thread  volatile
> Node()
    nextWaiter = null
    thread = null
> Node(Node mode)
    nextWaiter = mode  // mode == SHARED or EXCLUSIVE
    thread = Thread.currentThread()
> Node(int waitStatus)
    $.waitStatus = waitStatus
    thread = Thread.currentThread()
> isShared()
    return nextWaiter == SHARED
> isCancelled()
    return waitStatus > 0

AbstractQueuedSynchronizer::ConditionObject
// 条件量, 非static类
> await()