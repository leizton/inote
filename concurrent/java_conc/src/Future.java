/**
 * 同时实现Future和Runnable
 * 实现Runnable的用意是可以传给Executor.execute(run)执行
 * @see java.util.concurrent.AbstractExecutorService
 */
FutureTask
> 字段
    int state                  // state!=NEW时，isDone()返回true
    Callable<V> callable
    volatile Thread runner     // 执行this.callable的线程
    volatile WaitNode waiters
    Object outcome             // 结果
> 内部类
    WaitNode {
        volatile Thread thread
        volatile WaitNode next
        WaitNode() { thread = Thread.currentThread(); }
    }
> run() @override
    if this.state != NEW ||
        !UNSAFE.compareAndSwapObject(this, this.runnerOffset, null, Thread.currentThread())  // 把this.runner设成当前线程
        return
    try
        Callable<V> tmpCallable = this.callable  // 防止callable被并发修改
        if tmpCallable != null && state == NEW
            try
                V ret = tmpCallable.call()
                this.set(ret)  // this.outcome = ret
            catch Throwable ex
                this.setException(ex)
    finally
        runner = null
        if int s = state; s >= INTERRUPTING
            this.handlePossibleCancellationInterrupt(s)
> get():V
    int s = state
    if s <= COMPLETING
        s = this.awaitDone(false, 0)
    return this.report(s)  // this.outcome
> awaitDone(boolean timed, long nanos):int
    // timed确定是否有截止时间
    long deadline = timed ? System.nanoTime() + nanos : 0
    // 这个方法可能被多个线程同时执行
    // 每个线程执行时都有一个WaitNode
    WaitNode w = null
    boolean queued = false  // w是否已入队列
    while true
        if Thread.interrupted()
            this.removeWaiter(w)
            throw new InterruptedException()
        int s = state
        if s > COMPLETING
            if w != null: w.thread = null
            return s
        elif s == COMPLETING
            Thread.yield()
        elif w == null
            w = new WaitNode()
        elif !queued
            queued = UNSAFE.compareAndSwapObject(this, waitersOffset, w.next = this.waiters, w)
        elif timed
            nanos = deadline - System.nanoTime()  // 以纳秒作单位的时间值
            if nanos <= 0
                this.removeWaiter(w)
                return state
            LockSupport.parkNanos(this, nanos)
        else
            LockSupport.park(this)