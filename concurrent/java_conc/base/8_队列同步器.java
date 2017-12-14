ReentrantLock
// example
- sync  Sync
> ReentrantLock()
    sync = new NonfairSync()
> lock()
    sync.lock()
> unlock()
    sync.release(1)
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
> lock()
    if compareAndSetState(0, 1)
        setExclusiveOwnerThread(Thread.currentThread)
    else
        acquire(1)
> tryAcquire(int acquires):boolean
    return nonfairTryAcquire(acquires)


AbstractOwnableSynchronizer
// 用于跟踪当前是哪个线程独占了同步器
- exclusiveOwnerThread  Thread  当前独占同步器的线程
> setExclusiveOwnerThread(Thread)
> getExclusiveOwnerThread():Thread


AbstractQueuedSynchronizer
// 实现同步阻塞锁和如信号量等同步器的框架
- head   Node
- tail   Node
- state  int
// 占锁
> acquire(int arg)
    // tryAcquire()由子类实现, 子类通过对state的cas修改实现
    if tryAcquire(arg), return  // tryAcquire成功时, 不会入队
    waiter = addWaiter(Node.EXCLUSIVE)
    if !acquireQueued(waiter, arg), return
    Thread.currentThread().interrupt()
> addWaiter(Node mode):Node
    node = new Node(mode);
    enq(node)
    return node
> enq(Node node):Node
    while true
        t = tail
        if t == null
            if compareAndSetHead(null, new Node())  // 初始化空的头节点
                tail = head
        else
            node.prev = t
            if compareAndSetTail(t, node)
                t.next = node
                return t
> acquireQueued(Node node, int arg):boolean
    isFail = true, isInterrupted = false
    try {
        while true
            p = node.prev
            if p == head && tryAcquire(arg)  // 如果占锁且未入队的线程释放了锁, 这tryAcquire会成功
                setHead(node)
                p.next = null  // help gc
                isFail = false
                return isInterrupted
            if shouldParkAfterFailedAcquire(p, node)
                isInterrupted |= parkAndCheckInterrupt()
    } finally {
        if isFail, cancelAcquire(node)
    }
> setHead(Node node)
    head = node
    node.thread = node.prev = null
> shouldParkAfterFailedAcquire(Node prev, Node node):boolean
    // 返回当前线程是否应该休眠(park)
    int st = prev.waitStatus
    if st == Node.SIGNAL
        return true
    if st > 0
        // prev已经cancelled
        do
            node.prev = prev = prev.prev
        while prev.waitStatus > 0
        prev.next = node
    else
        // st == 0 or PROPAGATE
        prev.compareAndSetWaitStatus(st, Node.SIGNAL)
    return false
> parkAndCheckInterrupt():boolean
    LockSupport.park(this)  // 阻塞直到被unpark
    return Thread.interrupted()
// 释放锁
> release(int arg):boolean
    if tryRelease(arg)  // tryRelease()由子类实现
        // 完全释放锁了
        h = head
        // 如果h.waitStatus==0, h.next不会进入park, 在acquireQueued里继续tryAcquire
        if h != null && h.waitStatus != 0
            unparkSuccessor(h)  // unpark后继节点
        return true
    return false
> unparkSuccessor(Node node)
    st = node.waitStatus
    if st < 0
        node.compareAndSetWaitStatus(st, 0)
    nx = node.next
    if nx == null || nx.waitStatus > 0
        nx = null  // 这个nx是null, 或已经cancelled了, 所以无需关系这个后继节点
        for t = tail;  t != null && t != node;  t = t.prev
            if t.waitStatus <= 0, nx = t  // t未cancelled
    if nx != null
        LockSupport.unpark(nx.thread)

AbstractQueuedSynchronizer::Node
// 占锁mode
- SHARED      Node    static final  = new Node();
- EXCLUSIVE   Node    static final  = null;
// waitStatus取值
- CANCELLED   int     = 1
- SIGNAL      int     = -1      通知下一个节点可以进入休眠等待
- CONDITION   int     = -2
- PROPAGATE   int     = -3
//
- waitStatus  int     volatile  取值0表示next节点无需park, 直接tryAcquire
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