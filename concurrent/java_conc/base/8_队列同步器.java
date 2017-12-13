ReentrantLock
// example

Sync extends AbstractQueuedSynchronizer
> nonfairTryAcquire(int acquires):boolean  // 返回是否成功占锁
    // 如果没有线程占锁, 就尝试占锁, 尝试成功返回true, 否则返回false
    // 如果锁已被其他线程占, 返回false
    // 锁被自己占有, 对于可重入锁, 在state上加acquires, 表示再次加锁
    curr = Thread.currentThread()
    s = getState()
    if s == 0
        if compareAndSetState(0, acquires)  // 尝试占锁
            setExclusiveOwnerThread(curr)
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

NonfairSync extends Sync
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


AbstractQueuedSynchronizer
// 实现同步阻塞锁和如信号量等同步器的框架