class Acceptor(endPoint: EndPoint,
			   sendBufferSize: Int,
			   recvBufferSize: Int,
			   brokerId: Int,
			   processors: Array[Processor],  // 相关的processor
			   connectionQuotas: ConnectionQuotas)
	extends AbstractServerThread(connectionQuotas) {

	val nioSelector = java.nio.channels.Selector.open()
	val serverChannel = this.openServerSocket(endPoint.host, endPoint.port)

	this.synchronized {
		// 每个processor有1个IO线程
		processors.foreach { processor =>
			Utils.newThread(s"kafka-network-thread-$brokerId-${endPoint.listenerName}-${endPoint.securityProtocol}-${processor.id}",
							runnable=processor, daemon=false).start()
		}
	}

	override def run() {
		// 启动时，把serverChannel注册到nioSelector上
		this.serverChannel.register(this.nioSelector, SelectionKey.OP_ACCEPT)
		super.startupLatch.countDown()  // 通知SocketServer::startup()

		// selector轮询
		var currentProcessor = 0
		while super.isRunning  // Acceptor和Processor的shutdown由超类AbstractServerThread管理
			val ready = nioSelector.select(500)  // 超时时间500毫秒
			if ready <= 0 continue
			val keys = nioSelector.selectedKeys()
			val it = keys.iterator()
			while it.hasNext && isRunning
				val key = it.next()
				it.remove()
				if key.isAcceptable()
					this.accept(key, processors(currentProcessor))  // processors来自构造方法的输入参数
				else
					throw new IllegalStateException("unknown key state")
				currentProcessor = (currentProcessor + 1) % processors.length
	}

	def accept(key: SelectionKey, processor: Processor) {
		val serverSocketChannel = key.channel().asInstanceOf[ServerSocketChannel]
		val socketChannel = serverSocketChannel.accept()
		try {
			// 检查这个客户端是否有过多的连接
			// {connectionQuotas}是{SocketServer}的字段
			connectionQuotas.inc(socketChannel.socket().getAddress())
		} catch {
			// 连接满了
			case e: TooManyConnectionsException =>
				socketChannel.socket().close()
				socketChannel.close()
		}
		socketChannel.configureBlocking(false)
		socketChannel.setTcpNoDelay(true)  // 禁用Nagle算法
		socketChannel.setKeepAlive(true)
		if sendBufferSize != -1
			socketChannel.socket().setSendBufferSize(sendBufferSize)
		// 传给processor处理
		processor.accept(socketChannel)
	}
}