/*
 * DefaultAsyncHttpClient
*/
DefaultAsyncHttpClient
字段
	nettyTimer = new HashedWheelTimer()  // nettyTimer.start()
	channelManager = new ChannelManager(config, nettyTimer)
	requestSender = new NettyRequestSender(config, channelManager, nettyTimer, new AsyncHttpClientState(closed))
方法
	execute(Request request, AsyncHandler<T> asyncHandler):ListenableFuture {
		try
			// 发送请求, 并设置回调处理
			// @see NettyRequestSender
			return this.requestSender.sendRequest(request, asyncHandler, null, false)
		catch Exception e
			asyncHandler.onThrowable(e)  // 让异步处理器处理异常
			return new ListenableFuture.CompletedFailure<>(e)
	}


/*
 * NettyRequestSender
 */
NettyRequestSender
方法
	/*
	 * sendRequest()
	 * 发送请求。reclaimCache: 是否回收缓存
	 */
	sendRequest(Request request, AsyncHandler<T> asyncHandler,
				NettyResponseFuture<T> future=null, boolean reclaimCache=false):ListenableFuture<T> {
		// 调用 sendRequestWithCertainForceConnect 或 sendRequestThroughSslProxy
	}
	sendRequestWithCertainForceConnect(Request request, AsyncHandler<T> asyncHandler, NettyResponseFuture<T> future=null,
				boolean reclaimCache=false, ProxyServer proxyServer=null, boolean forceConnect=false):ListenableFuture<T> {
		// 先把 Request 转成 由HttpRequest(封装http版本/请求方法/uri)和NettyBody(封装请求包体)组成的NettyRequest;
		// 再创建另一个NettyResponseFuture.
		NettyResponseFuture<T> newFuture = this.newNettyRequestAndResponseFuture(request, asyncHandler, ...)

		// 当future不是null时, 才可能可以复用channel
		Channel channel = this.getOpenChannel(future, request, proxyServer, asyncHandler)
		if channel == null || !channel.isActive()
			return this.sendRequestWithNewChannel(request, proxyServer, newFuture, asyncHandler, reclaimCache)
		else
			return this.sendRequestWithOpenChannel(request, proxyServer, newFuture, asyncHandler, channel)
	}
	/*
	 * getOpenChannel()
	 * 依赖 ChannelManager::poll()
	 * ChannelManager::poll() 依赖 NoopChannelPool/DefaultChannelPool::poll
	 * channelManager管理channel池, channel池有2种: NoopChannelPool DefaultChannelPool
	 * 这2中channel池的poll方法不同
	 */
	getOpenChannel(NettyResponseFuture<?> future, request, proxyServer, asyncHandler):Channel {
		if future.reuseChannel() && future.channel().isActive()
			return future.channel()
		else
			return this.channelManager.poll(request.getUri(), request.getVirtualHost(), proxyServer, request.getChannelPoolPartitioning())
	}
	ChannelManager
		channelPool = !config.isKeepAlive() ? NoopChannelPool.INSTANCE : new DefaultChannelPool(config, nettyTimer)
		poll(uri, virtualHost, proxyServer, connectionPoolPartitioning) {
			Object partitionKey = connectionPoolPartitioning.getPartitionKey(uri, virtualHost, proxy)
			return channelPool.poll(partitionKey)  // 对于NoopChannelPool是直接返回null
		}
	DefaultChannelPool
		ConcurrentHashMap<Object, ConcurrentLinkedDeque<IdleChannel>> partitions
		poll(Object partitionKey):Channel {
			ConcurrentLinkedDeque<IdleChannel> partition = this.partitions.get(partitionKey)
			if (partition != null) {
					while IdleChannel channel = partition.pollFirst(), channel != null
					if channel.isActive()          // 未断开客户端的连接
					   && channel.takeOwnership()  // channel.owned.compareAndSet(false, true), 是否可以占用这个channel
					   return channel
			}
			return null
		}
	/*
	 * sendRequestWithNewChannel()
	 */
	sendRequestWithNewChannel(request, proxyServer, future, asyncHandler, reclaimCache):ListenableFuture<T> {
		// 请求超时
		this.scheduleRequestTimeout(future) {
			future.setTimeoutsHolder(new TimeoutsHolder(nettyTimer, future, this, config))  // 这个设计不合理, 有循环依赖
		}
		// 主机域名解析
		RequestHostnameResolver.INSTANCE.resolve(request, proxyServer, asyncHandler).addListener(
			new SimpleFutureListener<List<InetSocketAddress>>() {
				// 主机名解析成功
				onSuccess(List<InetSocketAddress> addresses) {  // 成功获得主机地址
					// NettyConnectListener
					NettyConnectListener<T> connectListener = new NettyConnectListener<>(
						future, NettyRequestSender.this/*在tcp连接建立后调用它的writeRequest(future, channel)方法*/,
						channelManager, channelPreempted=!reclaimCache/*不回收缓存则独占channel*/, future.getPartitionKey())
					// NettyChannelConnector
					NettyChannelConnector connector = new NettyChannelConnector(
						request.getLocalAddress()/*客户端地址*/, addresses/*远端地址*/, asyncHandler, clientState, NettyRequestSender.this.config)
					// 连接
					io.netty.bootstrap.Bootstrap bootstrap = channelManager.getBootstrap(request.getUri(), proxyServer)
					connector.connect(bootstrap, connectListener) {
						// bootstrap只是负责建立tcp连接
						bootstrap.connect(remoteAddress, localAddress).addListener(
							new SimpleChannelFutureListener() {
								// tcp连接成功后是onSuccess回调, 调用NettyRequestSender的writeRequest(future, channel)
								onSuccess(Channel channel) -> connectListener.onSuccess(channel, remoteAddress)
								onFailure(Channel channel, Throwable t) -> connectListener.onFailure(channel, t)
							}
						)
					}
				}
				// 主机名解析失败
				onFailure(Throwable cause) {}
			}
		)
	}
	/*
	 * sendRequestWithOpenChannel()
	 */
	sendRequestWithOpenChannel(request, proxyServer, future, asyncHandler, channel):ListenableFuture<T> {
		this.scheduleRequestTimeout(future)  // 请求超时
		this.writeRequest(future, channel)  // 已经建立过了tcp连接
		return future
	}
	/*
	 * writeRequest()
	 */
	writeRequest(future, channel) {
		NettyRequest nettyRequest = future.getNettyRequest()
		HttpRequest httpRequest = nettyRequest.getHttpRequest()
		if nettyRequest.getBody() != null && httpRequest.getMethod() != HttpMethod.connect
			// 有响应包体
			nettyRequest.getBody().write(channel, future)
		// 读超时
		this.scheduleReadTimeout(future) {
			TimeoutsHolder timeoutsHolder = nettyResponseFuture.getTimeoutsHolder()
			nettyResponseFuture.touch()
			timeoutsHolder.startReadTimeout()
		}
	}