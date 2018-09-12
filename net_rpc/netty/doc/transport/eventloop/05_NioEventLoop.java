AbstractEventExecutor
	implements EventExecutor
	extends AbstractExecutorService  // 实现EventExecutorGroup的父接口ExecutorService
> 字段
	EventExecutorGroup parent
> AbstractEventExecutor(EventExecutorGroup parent)
	this.parent = parent
// 实现接口-EventExecutor
> parent():EventExecutorGroup
	return parent
> newPromise:Promise<V>
	return new DefaultPromise<V>(this)
> newProgressivePromise():ProgressivePromise<V>
	return new DefaultProgressivePromise<V>(this)
> newSucceededFuture(V result):Future<V>
	new SucceededFuture<V>(this, result)
> newFailedFuture(Throwable cause):Future<V>
	new FailedFuture<V>(this, cause)
// 不支持ScheduledExecutorService接口
> schedule(Runnable run, long delay, TimeUnit unit):ScheduledFuture<?>
	throw new UnsupportedOperationException()
> schedule(Callable<V> call, long delay, TimeUnit unit):ScheduledFuture<V>
	throw new UnsupportedOperationException()


AbstractScheduledEventExecutor
	extends AbstractEventExecutor
> AbstractScheduledEventExecutor(EventExecutorGroup parent)
	AbstractEventExecutor::super(parent)
// 实现AbstractEventExecutor不支持的ScheduledExecutorService接口
> schedule(Runnable run, long delay, TimeUnit unit):ScheduledFuture<?>
	task = new ScheduledFutureTask<Void>(this, run, null, ScheduledFutureTask.deadlineNanos(unit.toNanos(delay)))
	return this.schedule(task)
> schedule(Callable<V> call, long delay, TimeUnit unit):ScheduledFuture<V>
	task = new ScheduledFutureTask<V>(this, call, null, ScheduledFutureTask.deadlineNanos(unit.toNanos(delay)))
	return this.schedule(task)
> schedule(ScheduledFutureTask<V> task):ScheduledFutureTask<V>
	if EventExecutor::inEventLoop(Thread.currentThread())
		scheduledTaskQueue().add(task)
	else
		Executor::execute(() -> scheduledTaskQueue().add(task))


/**
 * SingleThreadEventLoop的上一层
 */
SingleThreadEventExecutor
	extends AbstractScheduledEventExecutor
	implements EventExecutor
> 字段
	Queue<Runnable>  taskQueue
	Thread           thread
// 构造器
> SingleThreadEventExecutor(
		EventExecutorGroup parent, Executor executor,
		boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rh)
	AbstractScheduledEventExecutor::super(parent)
	this.executor = executor  // NioEventLoopGroup的构造方法的executor参数
	this.addTaskWakesUp = addTaskWakesUp
	this.maxPendingTasks = Math.max(16, maxPendingTasks)  // 至少16
	this.taskQueue = new LinkedBlockingQueue<Runnable>(this.maxPendingTasks)
	this.rejectedExecutionHandler = rh
// 维护任务队列
> addTask(Runnable task)
	if !offerTask(task)
		this.rejectedExecutionHandler.rejected(task, this)
> offerTask(Runnable task)
	taskQueue.offer(task)
> removeTask(Runnable task)
	taskQueue.remove(task)
> peekTask():Runnable
	return taskQueue.peek()
// 实现接口-Executor
> execute(Runnable task)
	boolean inEventLoop = inEventLoop(Thread.currentThread())
	if inEventLoop
		addTask(task)
	else
		startThread()
		addTask(task)
		if isShutdown() && removeTask(task)  // startThread()这个过程可能出错
			throw new RejectedExecutionException("event executor terminated")
	if !addTaskWakesUp && wakesUpForTask(task)
		wakeup(inEventLoop)
> startThread()
	// STATE_UPDATER是静态AtomicIntegerFieldUpdater<SingleThreadEventExecutor>，控制this.state字段
	if STATE_UPDATER.get(this) != ST_NOT_STARTED: return
	if !STATE_UPDATER.compareAndSet(this, ST_NOT_STARTED, ST_STARTED): return
	this.executor.execute(() -> {
		thread = Thread.currentThread()
		this.run()  // run()是一个抽象方法，由NioEventLoop实现
	})
// 实现接口-EventExecutor
> inEventLoop(Thread thread)
	return thread == this.thread


/**
 * NioEventLoop的上一层
 */
SingleThreadEventLoop
	extends SingleThreadEventExecutor
	implements EventLoop
> parent():EventLoopGroup
	return (EventLoopGroup) super.parent()
> next():EventLoop
	return (EventLoop) super.next()
// 实现接口-EventLoopGroup
> register(Channel channel):ChannelFuture
	return this.register(new DefaultChannelPromise(channel, this))
> register(final ChannelPromise promise):ChannelFuture
	promise.channel().unsafe().register(this, promise)
	return promise


NioEventLoop
	extends SingleThreadEventLoop
> 字段
	SelectedSelectionKeySet   selectedKeys
	Selector                  selector
	AtomicBoolean             wakenUp
	boolean                   needsToSelectAgain

> NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider,
			   SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler)
	super(parent, executor, false, DEFAULT_MAX_PENDING_TASKS, rejectedExecutionHandler)
	this.provider = selectorProvider  // NioEventLoopGroup的构造函数中，SelectorProvider.provider()
	this.selector = this.openSelector()
	this.selectStrategy = strategy

> openSelector()
	// unwrappedSelector 是 sun.nio.ch.SelectorImpl
	this.unwrappedSelector = this.provider.openSelector()
	if DISABLE_KEYSET_OPTIMIZATION  // 不做优化
		return unwrappedSelector
	// SelectorImpl的selectedKeys用HashSet实现，netty自定义SelectedSelectionKeySet替代HashSet
	this.selectKeys = new SelectedSelectionKeySet()
	unwrappedSelector.selectedKeys = this.selectKeys
	unwrappedSelector.publicSelectedKeys = this.selectKeys
	return unwrappedSelector

> run() @Override-SingleThreadEventExecutor
// 被SingleThreadEventExecutor::startThread()调用
	while true
		if SingleThreadEventExecutor::hasTask() &&
		   this.selectNowSupplier.get()==CONTINUE
			continue

		this.select(wakenUp.getAndSet(false))  // 取出wakenUp的旧值，新值赋false
		if wakenUp.get()
			this.selector.wakenUp()

		this.needsToSelectAgain = false
		long ioTime = System.nanoTime()  // 记录I/O用时
		this.processSelectedKeys()
		ioTime = System.nanoTime() - ioTime
		if this.ioRatio == 100  // ioRatio是I/O用时百分比，默认50
			SingleThreadEventExecutor::runAllTasks()
		else
			SingleThreadEventExecutor::runAllTasks(timeoutNanos = ioTime * (100 - ioRatio) / ioRatio)

> select(boolean oldWakeUp)
	Selector selector = this.selector
	long selectCount = 0
	long currentTime = System.nanoTime()
	final long selectDeadLineNanos = currentTimeNanos + delayNanos(currentTimeNanos)
	for ; true; currentTimeNanos=System.nanoTime()
		long timeoutMillis = (selectDeadLineNanos - currentTimeNanos + 500000L) / 1000000L
		if timeoutMillis <= 0
			if selectCount > 0
				return  // 执行过selector.select(timeoutMillis)
			break  // 去执行selectNow()
		if SingleThreadEventLoop::hasTasks() && wakenUp.compareAndSet(false, true)
			return

		// select(timeoutMillis)可能在timeoutMillis时间内返回，所以外层用循环
		int selectedKeys = selector.select(timeoutMillis)
		selectCount++

		if selectedKeys > 0 || oldWakenUp || wakenUp.get() || hasTasks() || hasScheduledTasks()
			// 有事件发生；被唤醒；有任务要执行
			return
		if System.nanoTime() - currentTimeNanos >= timeoutMillis * 1000000L
			return  // selector.select(timeoutMillis)超时，没有事件发生

		if selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD(默认是512)
			// 绕过java nio的epoll导致cpu占用100%问题
			// 创建一个新的selector，并把旧selector的key转移到新selector上
			this.rebuildSelector()
			selector = this.selector  // 更新成新的selector
			break  // 去执行selectNow()
	selector.selectNow()

> processSelectedKeys()
	if this.selectedKeys == null
		processSelectedKeysPlain(this.selector.selectedKeys())
	else
		// openSelector()时有做优化
		processSelectedKeysOptimized()

> processSelectedKeysPlain(Set<SelectionKey> selectedKeys)
	Iterator<SelectionKey> it = selectedKeys.iterator()
	while it.hasNext()
		SelectionKey k = it.next()
		Object a = it.next().attachment()
		it.remove()

		if a instanceof AbstractNioChannel
			processSelectedKey(k, (AbstractNioChannel) a)
		else
			processSelectedKey(k, (NioTask<SelectableChannel>) a)

		if this.needsToSelectAgain && it.hasNext()
			/**
			 * 避免ConcurrentModificationException
			 * 获取hashSet的迭代器后，如果通过调用迭代器的remove()以外的方式来修改hashSet，会抛出异常
			 * @see java.util.HashSet
			 */
			needsToSelectAgain = false
			this.selector.selectNow()
			it = selector.selectedKeys()

> processSelectedKey(SelectionKey k, AbstractNioChannel ch)

> processSelectedKey(SelectionKey k, NioTask<SelectableChannel> task)