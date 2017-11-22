KafkaRequestHandlerPool
	// KafkaServer::startup()里创建和初始化
	(Int brokerId, RequestChannel requestChannel, KafkaApis apis, Int threadNum)

> 初始化
	val threads = new Array[Thread](threadNum)
	val runnables = new Array[KafkaRequestHandler](threadNum)
	// 启动处理线程
	for i <- 0 until threadNum
		runnables(i) = new KafkaRequestHandler(i, requestChannel, apis)
		threads(i) = new Thread(runnables(i))
		threads(i).start()

> shutdown()
	for handle <- runnables
		handle.shutdown()
	for th <- threads
		th.join()




KafkaRequestHandler
	(id, RequestChannel requestChannel, KafkaApis #apis)
	implements Runnable

> run()
	while true
		var req: RequestChannel.Request
		while req == null
			req = this.requestChannel.requestQueue.poll(300)
		if req.equals(RequestChannel.AllDone)
			return
		#api.handle(req)

> shutdown()
	// 添加结束标志请求
	this.requestChannel.requestQueue.put(RequestChannel.AllDone)