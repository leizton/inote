Dispatcher
	事件分发器
Tip:
	Subscriber.dispatchEvent(event)是非阻塞方法.
内部类
	这3个内部类都是Dispatcher的实现.
	ImmediateDispatcher 立即
		extends Dispatcher
	方法
		@Override dispatch(Object event, Iterator<Subscriber> subscribers) {
			checkNotNull: event
			while subscribers.hasNext()
				subscribers.next().dispatchEvent(event)
		}
	LegacyAsyncDispatcher  // 遗留的异步方式, 现在用PerThreadQueuedDispatcher
		extends Dispatcher
	内部类
		EventWithSubscriber
		字段: Object event, Subscriber subscriber
	字段
		ConcurrentLinkedQueue<EventWithSubscriber> queue = Queues.newConcurrentLinkedQueue()
	方法
		@Override dispatch(Object event, Iterator<Subscriber> subscribers) {
			checkNotNull: event
			while subscribers.hasNext()
				queue.add( new EventWithSubscriber(event, subscribers.next()) )  // 入队
			while e = queue.poll(), e != null  // 出队
				e.subscribers.dispatchEvent(e.event)
		}
	PerThreadQueuedDispatcher  // 每个线程有一个队列, 保证送到同一个线程的所有event按送到线程的顺序分发到事件分析器
		extends Dispatcher
	内部类
		Event
		字段: Object event, Subscriber subscriber
	字段
		final ThreadLocal<Queue<Event>> queueLocal =  // 不是static
			new ThreadLocal<Queue<Event>>()
				@Override # initialValue():Queue<Event> { return Queues.newArrayDeque() }
		final ThreadLocal<Boolean> dispatchingLocal =  // 是否正在执行dispatch
			new ThreadLocal<Boolean>()
				@Override # initialValue():Boolean { return false }
	方法
		@Override dispatch(Object event, Iterator<Subscriber> subscribers) {
			checkNotNull: event, subscribers
			Queue<Event> q = queueLocal.get()
			q.offer( new Event(event, subscribers) )
			if !dispatchingLocal.get()  // 为什么要加这个ThreadLocal型的标志位
				dispatchingLocal.set(true)  // 是线程安全的
				try
					while e = q.poll(), e != null
						while e.subscribers.hasNext()
							e.subscribers.next().dispatchEvent(e.event)
				finally
					dispatchingLocal.remove()
					queueLocal.remove()
		}
抽象方法
	abstract dispatch(Object event, Iterator<Subscriber> subscribers)
静态方法
	immediate() { return ImmediateDispatcher.INSTANCE }
	legacyAsync() { return new LegacyAsyncDispatcher() }
	perThreadDispatchQueue() { return new PerThreadQueuedDispatcher() }