SubscriberRegistry
字段
	EventBus bus
	ConcurrentMap<Class<?>, CopyOnWriteArraySet<Subscriber>> subscribers = Maps.newConcurrentMap()
构造器
	SubscriberRegistry(EventBus bus) { this.bus = checkNotNull(bus) }
静态方法
	getAnnotatedMethods(Class<?> clazz):ImmutableList<Method> {
		return subscriberMethodsCache.getUnchecked(clazz)
	}
静态方法
	getAnnotatedMethodsNotCached(Class<?> clazz):ImmutableList<Method>
方法
	register(Object listener) {
		/* 查找出listener的有@Subscribe注解的方法, 由Method对象 listener bus创建出Subscriber.
		 * Method对象的第一个参数的Class<?>是时间类型, 用作listenerMethods的Key */
		Multimap<Class<?>, Subscriber> listenerMethods = findAllSubscribers(listener)
		
		for Map.Entry<Class<?>, Collection<Subscriber>> e : listenerMethods.asMap().entrySet()
			Class<?> eventType = e.getKey()
			Collection<Subscriber> eventMethodsInListener = e.getValue()
			
			CopyOnWriteArraySet<Subscriber> eventSubscribers = this.subscribers.get(eventType)
			if eventSubscribers == null
				CopyOnWriteArraySet<Subscriber> newSet = new CopyOnWriteArraySet()
				eventSubscribers = MoreObjects.firstNonNull(
						subscribers.putIfAbsent(eventType, newSet), newSet)
			eventSubscribers.addAll(eventMethodsInListener)  // 加入到subscribers中
	}
	findAllSubscribers(Object listener) {
		Multimap<Class<?>, Subscriber> methodsInListener = HashMultimap.create()
		Class<?> clazz = listener.getClass()
		for Method method : getAnnotatedMethods(clazz)  // 遍历有@Subscribe注解的方法
			Class<?>[] parameterTypes = method.getParameterTypes()
			Class<?> eventType = parameterTypes[0]
			// 因为在listener上可能有多个方法订阅了事件, 所以使用Multimap
			methodsInListener.put( eventType, Subscriber.create(this.bus, listener, method) )
	}