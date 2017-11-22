EventBus
	是观察者模式/发布者订阅者模式的实现.
字段
	String identifier
	Executor executor
	Dispatcher dispatcher
	SubscriberExceptionHandler exceptionHandler
	SubscriberRegistry subscribers = new SubscriberRegistry(this)  // 订阅者注册
构造器 有4个
	EventBus(String identifier) {
		/* MoreExecutors.directExecutor()返回DirectExecutor.INSTANCE,
		 * 执行器是DirectExecutor, 直接执行Runnable对象的run()方法.
		 * 事件分发器是PerThreadQueuedDispatcher, 保证送到同一个线程的事件按序分发出去. */
		this(identifier, MoreExecutors.directExecutor(),
				Dispatcher.perThreadDispatchQueue(), LoggingHandler.INSTANCE)
	}
	EventBus(String identifier, Executor executor, Dispatcher dispatcher,
			SubscriberExceptionHandler exceptionHandler)  // 设置4个字段的值
方法
	register(Object object) { subscribers.register(object) }
	unregister(Object object) { subscribers.unregister(object) }
	