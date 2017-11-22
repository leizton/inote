// Processor是一个Runnable，处理连接
class Processor (processorId: Int,
				 time: Time,
				 maxRequestSize: Int,
				 requestChannel: RequestChannel,
				 connectionQuotas: ConnectionQuotas,
				 connectionsMaxIdleMs: Long,
				 listenerName: ListenerName,
				 securityProtocol: SecurityProtocol,
				 channelConfigs: java.util.Map[String, _],
				 metrics: Metrics,
				 credentialProvider: CredentialProvider)
	extends AbstractServerThread(connectionQuotas) with KafkaMetricsGroup {

	val newConnections = new ConcurrentLinkedQueue[SocketChannel]()

	// KSelector 实际是 org.apache.kafka.common.network.Selector
	// Selector 的构造函数里 this.nioSelector = java.nio.channels.Selector.open()
	val selector = new KSelector(maxRequestSize, connectionsMaxIdleMs, metrics, time, "socket-server", metricTags, false,
								 ChannelBuilders.serverChannelBuilder(securityProtocol, channelConfigs, credentialProvider.credentialCache))

	/**
	 * accept()是生成者，run()是消费者
	 * newConnections是实现关联的数据结构
	 * accept()把Acceptor传来的SocketChannel放入newConnections，由run()中的configureNewConnections()消费
	 */
	def accept(channel: java.nio.channels.SocketChannel) {
		this.newConnections.add(channel)
		this.selector.wakeup()
	}

	override def run() {  // 实现-Runnable
		super[AbstractServerThread].startupComplete()
		while super.isRunning
			configureNewConnections()   // step-1 把新的SocketChannel注册到selector上
			processNewResponses()       // step-4 发送{requestChannel.responseQueues}里的每个响应
			this.selector.poll(300)     // step-2 建立连接，读、写数据(经握手验权后建立新连接，可读时读取一个完整的NetworkReceive，可写时写Send)
			processCompletedReceives()  // step-3 接收到完整的NetworkReceive后，构造新的{RequestChannel.Request}，并放入{requestChannel.requestQueue}
			processCompletedSends()     // step-5 把发送完send的channel调成非静默
			processDisconnected()
	}

	// 从newConnections里取出channel，注册到selector
	def configureNewConnections() {
		while !newConnections.isEmpty
			val socketChannel = this.newConnections.poll()
			val localHost, remoteHost, ... = socketChannel.socket().getLocalAddress.getHostAddress, socketChannel.socket().getInetAddress.getHostAddress, ...
			// 用<localHost, localPort, remoteHost, remotePort>(套接字)构成一个连接的唯一标识
			val connectionId: String = ConnectionId(localHost, remoteHost, ...).toString
			this.selector.register(connectionId, socketChannel)
	}

	/**
	 * {requestChannel}是{SocketServer}的字段
	 * {requestChannel.requestQueue}
	 */
	def processCompletedReceives() {
		this.selector.completedReceives.foreach(NetworkReceive receive)
			String kChannelId = receive.source
			try
				val channel = selector.channels.get(kChannelId)
				if channel == null: channel = selector.closingChannels.get(kChannelId)
				val req = RequestChannel.Request(processor=this.processorId,
												 connectionId=receive.source,
												 buffer=receive.buffer, ...)
				requestChannel.requestQueue.put(req)
				this.selector.mute(receive.source)  // kafkaChannel.mute()，暂停接收数据
			catch Exception e
				this.close(kChannelId)
	}

	/**
	 * {requestChannel}是{SocketServer}的字段
	 * {requestChannel.responseQueues}存放每个processor的responseQueue
	 */
	def processNewResponses() {
		var curr: RequestChannel.Response
		// 尝试从队列中取Response
		while ( curr = requestChannel.responseQueues(this.processorId).poll() ) != null
			curr.responseAction match {
				case RequestChannel.NoOpAction =>
					selector.unmute(curr.request.connectionId)  // 解除静默
				case RequestChannel.SendAction =>
					if selector.channels.containsKey(curr.responseSend.destination)  // channel还未关闭
						selector.send(curr.responseSend)   // kafkaChannel.setSend(send)
				case RequestChannel.CloseConnectionAction =>
					this.close(curr.request.connectionId)  // 关闭channel
			}
	}

	def processCompletedSends() {
		// 写数据流程：KSelector.poll() => KSelector.pollSelectionKeys() =isWritable()=> KafkaChannel.write()
		// 一个{send}发送完成后放入{selector.completedSends}
		this.selector.completedSends.foreach(Send send)
			this.selector.unmute(send.destination/* kafkaChannel id */)  // kafkaChannel.unmute()，恢复接收数据
	}

	def close(channelId: String) {
		val ch = selector.channels.get(channelId)
		if ch != null
			connectionQuotas.dec(ch.socketAddress)  // @see Acceptor::accept(), connectionQuotas用于限制同一个client的连接数
			selector.close(ch, false)
	}
}



// KSelector
Selector
> 字段
	java.nio.channels.Selector                nioSelector = Selector.open()
	Map<String, KafkaChannel>                 channels
	Map<String, KafkaChannel>                 closingChannels
	//
	Map<KafkaChannel, Deque<NetworkReceive>>  stagedReceives     // 每个channel是多次接收数据
	List<NetworkReceive>                      completedReceives
	//
	List<Send>                                completedSends
	List<String>                              connected, disconnected, failedSends
	//
	Set<SelectionKey>                         immediatelyConnectedKeys

/**
 * Processor::configureNewConnections()调用register()注册新channel
 */
> register(String channelId, SocketChannel socketChannel)
	KafkaChannel kafkaChannel = channelBuilder.buildChannel(channelId, key, maxReceiveSize)
	this.channels.put(channelId, kafkaChannel)
	SelectionKey key = socketChannel.register(this.nioSelector, SelectionKey.OP_READ)
	key.attach(kafkaChannel)

> poll(long timeout)
	clear()
	if hasStagedReceives() || !immediatelyConnectedKeys.isEmpty()
		// 由于有addToCompletedReceives()，使得 hasStagedReceives()==true 几乎不会发生，如果发生说明某个地方跑飞了
		timeout = 0
	// nioSelector.select()
	int ready = select(timeout)
		=> return timeout == 0L ? this.nioSelector.selectNow() : this.nioSelector.select(timeout)
	//
	if ready > 0 || !immediatelyConnectedKeys.isEmpty
		pollSelectionKeys(this.nioSelector.selectedKeys(), false, endSelect)
		pollSelectionKeys(immediatelyConnectedKeys, true, endSelect)
	//
	addToCompletedReceives()

> clear()
	this.completedSends/completedReceives/connected/disconnected.clear()
	closingChannels.entrySet.foreach(String key=channel.id(), KafkaChannel channel)
		Deque<NetworkReceive> deque = stagedReceives.get(channel)
		boolean sendFailed = failedSends.remove(channel.id())
		if deque空 || sendFailed
			doClose(channel, true)
			closingChannels.remove(key)
	disconnected.addAll(failedSends)
	failedSends.clear()

> pollSelectionKeys(Iterable<SelectionKey> selectionKeys, boolean isImmediatelyConnected, long currentTimeNanos)
	Iterator<SelectionKey> it = selectionKeys.iterator()
	while it.hasNext()
		SelectionKey key = it.next()
		it.remove()
		KafkaChannel channel = key.attachment()
		try
			if 	isImmediatelyConnected || key.isConnectable()/* (readyOps() & OP_CONNECT) != 0 */
				if channel.finishConnect()
					this.connected.add(channel.id())
				else
					continue
			if channel.isConnected() && !channel.ready()
				channel.prepare()  // 握手、验权建立连接，准备读取数据
					=> transportLayer.handshake(); authenticator.authenticate()
			if channel.ready() && key.isReadable() && !this.stagedReceives.containsKey(channel)
				while (NetworkReceive r = channel.read()) != null
					// 只会在这里对{stagedReceives}添加NetworkReceive
					// 随后执行addToCompletedReceives()，又把channel从stagedReceives移走
					addToStagedReceives(channel, r)
						=> stagedReceives.putIfAbsent(channel, new ArrayDeque)
						   stagedReceives.get(channel).add(r)
			if channel.ready() && key.isWritable()
				Send send = channel.write()  // 非阻塞
				if send != null
					this.completedSends.add(send)  // send已经完整地发送出去
			if !key.isValid()
				close(channel, true)
		catch Exception e
			close(channel, true)

/**
 * 把{stagedReceives}的NetworkReceive转到{completedReceives}，并从{stagedReceives}中移除
 */
> addToCompletedReceives()
	if this.stagedReceives.isEmpty()
		return
	it = stagedReceives.entrySet().iterator()
	while it.hasNext()
		KafkaChannel channel, Deque<NetworkReceive> deque = it.next()
		if !channel.isMute()
			this.completedReceives.add(deque.poll())
			if deque.isEmpty()
				it.remove()

> unmute(String channelId)
	KafkaChannel ch = this.channels.get(channelId)
	if ch == null
		ch = this.closingChannels.get(channelId)
	if ch != null
		ch.unmute()

> send(Send send)
	if this.closingChannels.containsKey(send.destination)
		this.failedSends.add(send)
	else
		// channel会添加{OP_WRITE}事件
		this.channels.get(send.destination).setSend(send)