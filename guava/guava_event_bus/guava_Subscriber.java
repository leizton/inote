Subscriber
字段
	EventBus bus
	Object target
	Method method
	Executor executor
静态方法
	create(EventBus bus, Object listener, Method method):Subscriber {
		/* Subscriber的构造器里:
		 * target = listener;  listener是观察者, 观察者的method方法处理event这个事件
		 * executor = bus.executor();  bus用于获取executor */
		return isDeclaredThreadSafe(method)  // method上是否加 @AllowConcurrentEvents 注解
				? new Subscriber(bus, listener, method)
				: new SynchronizedSubscriber(bus, listener, method)
	}
方法
	// 异步
	dispatchEvent(final Object event) {
		// 如果executor是Thread, 则是异步调用
		executor.execute( new Runnable() {
			try
				invokeSubscriberMethod(event);
			catch InvocationTargetException e
				bus.handleSubscriberException( e.getCause(), context(event) );
		});
	}
	invokeSubscriberMethod(Object event) {
		checkNotNull: event
		method.invoke(target, event)
	}
内部类
	SynchronizedSubscriber
		extends Subscriber
	方法
		@Override invokeSubscriberMethod(Object event) {
			synchronized (this) {
				super.invokeSubscriberMethod(event)
			}
		}