class Foo {
	private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private final EventBus eventBus = new EventBus("bus_id_1");

	public Object doAwait(Object param, long timeout) {
		FooSubscriber sub = new FooSubscriber()
		eventBus.register(sub)    // 注册订阅者  register
		doNoBlock(param)          // 无阻塞
		Thread.sleep(timeout)     // 超时等待结果
		eventBus.unregister(sub)  // 解除订阅者  unregister
		return sub.result
	}

	private void doNoBlock(Object param) {
		executor.execute(() -> {
			Thread.sleep(1000)
			eventBus.post(param)  // 发布事件结果
		})
	}

	private Class FooSubscriber {
		private Object ret

		// 事件回调
		@Subscriber
		public void onEvent(Object ret) {
			this.ret = ret
		}
	}
}