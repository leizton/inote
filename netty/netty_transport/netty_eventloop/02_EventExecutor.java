/**
 * EventLoop的上一层
 */
EventExecutor
	extends EventExecutorGroup
> parent():EventExecutorGroup
> inEventLoop(Thread thread):boolean
> newPromise():Promise<V>
> newSucceededFuture(V result):Future<V>
> newFailedFuture(V result):Future<V>