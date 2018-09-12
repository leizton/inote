EventLoopGroup
    extends EventExecutorGroup
> next():EventLoop
    // EventExecutorGroup::next()返回EventExecutor
    // 这里返回EventExecutor的子接口EventLoop
> register(Channel channel):ChannelFuture
> register(ChannelPromise promise):ChannelFuture


MultithreadEventLoopGroup
    extends MultithreadEventExecutorGroup
    implements EventLoopGroup
> 静态字段
    int DEFAULT_EVENT_LOOP_THREADS  // 默认线程数，2 * Runtime.getRuntime().availableProcessors()
> MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args)
    nThreads = nThreads <= 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads
    MultithreadEventExecutorGroup::super(nThreads, executor, args)
// 实现接口-EventLoopGroup
> next():EventLoop
    return (EventLoop) MultithreadEventExecutorGroup::super.next()  // 从children里选择
> register(Channel channel):ChannelFuture
    return next().register(channel)  // 实际由NioEventLoop的上一层SingleThreadEventLoop实现
> register(ChannelPromise promise):ChannelFuture
    return next().register(promise)  // 实际由NioEventLoop的上一层SingleThreadEventLoop实现


NioEventLoopGroup
    extends MultithreadEventLoopGroup
> NioEventLoopGroup()
    /**
     * MultithreadEventLoopGroup负责nThreads
     * MultithreadEventExecutorGroup负责创建executor，ThreadPerTaskExecutor(每个任务都用一个新线程来执行)
     * MultithreadEventExecutorGroup把[selectorProvider, selectStrategyFactory, rejectedExecutionHandler]
     *                              传给newChild()创建执行器-NioEventLoop
     */
    super(nThreads = 0,
          evecutor = null,
          selectorProvider = SelectorProvider.provider(),
          selectStrategyFactory = DefaultSelectStrategyFactory.INSTANCE,
          rejectedExecutionHandler = RejectedExecutionHandlers.reject())
> newChild(Executor executor, Object... args):EventExecutor
	return new NioEventLoop(this, executor,
							(SelectorProvider) args[0],
							((SelectStrategyFactory) args[1]).newSelectStrategy(),
							(RejectedExecutionHandler) args[2])


/**
 * netty的这一套设计得过于复杂，一方面是抽象出过多层，
 * 另一方面是继承关系很乱，比如EventExecutor继承EventExecutorGroup，相当于部分继承整体这很怪异。
 */