ThreadPoolExecutor
> ctl字段
    /**
     * 用一个整数打包(pack)了任务数(workerCount)和线程池的运行状态(runState)，
     * 所以最大线程数不是2^31-1，而是2^29-1。
     * runState: RUNNING_接受新任务并执行已入队的任务、SHUTDOWN_不接受新任务但执行已入队的任务、
     *           STOP_不接受新任务也不执行已入队的任务、TIDYING_所有任务已经结束workCount是0、
     *           TERMINATED_terminated()方法已被调用
     *
     * ctlOf(int runState, int workerCount):int = runState | workerCount
     * runStateOf(ctl) 返回状态
     * workerCountOf(ctl) 返回任务数
     * isRunning(ctl):boolean = ctl < SHUTDOWN
     */
    ctl = new AtomicInteger(ctlOf(RUNNING, 0))
> execute(Runnable runner)
    int c = ctl.get()
    if workerCountOf(c) < corePoolSize
        // 有空闲线程
        if addWorker(runner, true)
            return
        c = ctl.get()
    // 没有空闲线程
    if isRunning(c) && $.workQueue.offer(runner)
        // workQueue是构造方法里的阻塞队列
        // workQueue未满可以放入任务，此时不创建新线程
        int recheck = ctl.get()
        if !isRunning(recheck) && remove(runner)
            reject(runner)  // 回调构造方法传入的RejectHandle
        else if workerCountOf(recheck) == 0
            addWorker(null, false)
    else if !addWorker(runner, false)
        reject(runner)
> remove(Runnable runner)
    boolean remove = $.workQueue.remove(runner)  // 移出工作队列
    tryTerminate()
    return remove
> reject(Runnable runner)
    $.handler.rejectedExecution(runner, this)
> addWorker(Runnable firstTask, boolean core)
    // firstTask是新worker的第一个task
    // 当task执行完后, worker调用getTask()从workQueue里取task执行
    // 若获取task超时(keepAlive参数), 则worker线程执行完毕退出
    // @ref Worker::runWorker()
    retry:
    while 1
        int c = ctl.get(), rs = runStateOf(c), wc = workerCountOf(c)
        if rs > SHUTDOWN ||
           rs == SHUTDOWN && (firstTask != null || workQueue.isEmpty())
            return false  // 已经shutdown了, 所以不add
        while 1
            if wc >= (core ? corePoolSize : maximumPoolSize)
                return false
            if compareAndIncrementWorkerCount(c)
                break retry  // 成功增加workerCount, 跳出第一层while
            c = ctl.get()  // 增加workerCount失败
            if runStateOf(c) != rs
                continue retry  // 状态改变，重新检查是否shutdown了
    boolean workerStarted = workerAdded = false
    Worker w = null
    try {
        w = new Worker(firstTask);  // 创建新线程
        if w.thread == null
            return false
        // 再次检查是否shutdown
        $.mainLock.lock();
        try {
            rs = runStateOf(ctl.get())
            if rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)
                $.workers.add(w)  // workers是所有正在运行的任务
                workerAdded = true
        } finally {
            $.mainLock.unlock()
        }
        // 没有shutdown, 可以启动执行线程
        if workerAdded
            t.start()
            workerStarted = true
    } finally {
        if !workerStarted
            addWorkerFailed(w)
    }
    return workerStarted