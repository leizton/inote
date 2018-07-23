class SocketServer(val config: KafkaConfig,
				   val metrics: Metrics,
				   val time: Time,
				   val credentialProvider: CredentialProvider) {

	val endpoints = config.listeners.map(l => l.listenerName -> l).toMap  // Seq[EndPoint] 转成 Map[String, EndPoint]
	val numProcessorThreads = config.numNetworkThreads
	val totalProcessorThreads = endpoints.size * numProcessorThreads

	/**
	 * 每个{endpoint} 有 1个{acceptor}, {numProcessorThreads}个{processor}
	 * 总共有{totalProcessorThreads}个{processor}
	 * 所有的{processor}共用同一个{requestChannel}
	 */
	val requestChannel = new RequestChannel(totalProcessorThreads, config.queuedMaxRequests)
	val processors = new Array[Processor](totalProcessorThreads)
	val acceptors = mutable.Map[EndPoint, Acceptor]()  // acceptor和endpoint是一对一的关系

	var connectionQuotas: ConnectionQuotas = _

	// 启动
	def startup() {
		this.synchronized {
			// 连接配额
			this.connectionQuotas = new ConnectionQuotas(maxConnectionsPerIp, maxConnectionsPerIpOverrides)

			var begin = 0
			config.listeners.foreach {
				endpoint =>
					// 创建processors
					// 每个endpoint都有numProcessorThreads个processor
					for i <- 0 until this.numProcessorThreads
						var id = begin + i
						processor = new Processor(id, ..., $.requestChannel, ...)
						$.processors(id) = processor
						$.requestChannel.addProcessor(processor)
					begin += this.numProcessorThreads

					// 创建acceptor
					// 每个endpoint有1个acceptor
					val acceptor = new Acceptor(endpoint, config.socketSendBufferBytes, config.socketRecvBufferSize, config.brokerId,
												this.processors.slice(processorBeginIndex, processorEndIndex),  // 关联processors
												connectionQuotas)
					this.acceptors.put(endpoint, acceptor)

					// acceptor实现了Runnable接口
					// 每个acceptor有1个reactor线程
					Utils.newThread(s"kafka-socket-acceptor-$listenerName-$securityProtocol-${endpoint.port}", acceptor, false).start()
					acceptor.startupLatch.await()  // 阻塞地等待acceptor的run()方法被调用，执行了startupComplete()
			}
		}
	}
}