EventExecutorGroup
	extends ScheduledExecutorService  // 线程池
	extends Iterable<EventExecutor>
> isShuttingDown():boolean
> shutdownGracefully():Future<?>  // 优雅地关闭线程池
> next():EventExecutor  // 返回下一个线程执行器，这里违背了依赖倒置原则
> iterator():Iterator<EventExecutor> @override-Iterable


/**
 * 实现ScheduledExecutorService及其父接口
 * 实现方式就是简单地调用EventExecutorGroup::next()返回的EventExecutor的相应方法
 * 因为EventExecutor继承了EventExecutorGroup，所以可以调用它的那些方法
 */
AbstractEventExecutorGroup
	implements EventExecutorGroup
> submit(Runnable task):Future<?> @Override-ExecutorService
	return next().submit(task)


/**
 * 主要是定义了几个构造方法来创建children和chooser字段
 */
MultithreadEventExecutorGroup
	extends AbstractEventExecutorGroup
> 字段
	EventExecutor[]       children  // IO线程池
	EventExecutorChooser  chooser
> MultithreadEventExecutorGroup(int nThreads, Executor executor, Object... args)
	if executor == null
		// 默认的执行器是ThreadPerTaskExecutor
		// 每次runnable(task)，都调用线程工厂创建新的Thread来执行。DefaultThreadFactory就是一个线程工厂
		executor = new ThreadPerTaskExecutor(newDefaultThreadFactory())
	// children
	this.children = new EventExecutor[nThreads]
	for i : [0, nThreads)
		children[i] = this.newChild(executor, args)
	// chooser代表某种选择策略，从传入的数组children中选出一个EventExecutor，@see this.next()
	this.chooser = DefaultEventExecutorChooserFactory.INSTANCE.newChooser(children)
> next():EventExecutor @Override-EventExecutorGroup
	/**
	 * DefaultEventExecutorChooserFactory有2种选择器，就是简单地按轮流选择
	 * children长度是2的幂次时，children[idx.getAndIncrement() & (children.length - 1)]
	 * children长度不是2的幂次时，children[Math.abs(idx.getAndIncrement() % children.length)]
	 */
	this.chooser.next()
> newChild(Executor executor, Object... args):EventExecutor
	/**
	 * 创建执行器，执行器在构造NioEventLoopGroup对象时就创建好了。
	 * nThreads在-MultithreadEventLoopGroup-里有默认值是cpu核数的2倍。
	 * 这个抽象方法具体由-NioEventLoopGroup-实现。
	 */