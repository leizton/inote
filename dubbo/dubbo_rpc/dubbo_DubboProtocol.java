DubboProtocol
	package com.alibaba.dubbo.rpc.protocol.dubbo
	extends AbstractProtocol

方法
	/*
		服务端导出服务
		+ export,  DubboExporter<T>
			+ openServer,  ExchangeServer
				+ createServer
			< Exchangers.bind(url, requestHandler)
	*/
	export(Invoker<T> invoker):Exporter<T> throws RpcException {
		URL url = invoker.getUrl()
		String key = super.serviceKey(url)  // key="serviceGroup/serviceName:serviceVersion:port"
		DubboExporter<T> exporter = new DubboExporter<T>(invoker, url, super.exporterMap)
		super.exporterMap.put(key, exporter)  // 用于destory()停止服务
		
		// stub 存根
		Boolean isStubSupportEvent = url.getParameter(Constants.STUB_EVENT_KEY, false)  // url中没有设置该参数时用默认值false
		Boolean isCallbackservice = url.getParameter(Constants.IS_CALLBACK_SERVICE, false)
		if isStubSupportEvent && !isCallbackservice
			String stubServiceMethods = url.getParameter(Constants.STUB_EVENT_METHODS_KEY)
			super.stubServiceMethodsMap.put(url.getServiceKey(), stubServiceMethods)
		
		openServer(url)
		return exporter
	}
	// 根据 url 从 serverMap 获取ExchangeServer, 或者创建新的Server
	openServer(URL url) {
		String key = url.getAddress()
		boolean isServer = url.getParameter(Constants.IS_SERVER_KEY, true)
		if isServer
			ExchangeServer server = this.serverMap.get(key)
			if server == null
				serverMap.put(key, createServer(url))
			else
				server.reset(url)
	}
	// 创建新的Server，Server的实现类是ExchangeServer
	createServer(URL url):ExchangeServer {
		url = url.addParameterIfAbsent("channel.readonly.sent", "true")  // 默认server关闭时发送readonly事件
		url = url.addParameterIfAbsent("heartbeat", "60 * 1000")         // 默认开启heartbeat
		url = url.addParameter("codec", "dubbo")

		// 选择服务的底层实现，默认用netty
		String serverStr = url.getParameter("server", "netty")
		ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(serverStr)
		
		// 启动服务，@see dubbo_Exchangers_Exchanger.java
		ExchangeServer server = Exchangers.bind(url, this.requestHandler)
		
		String clientStr = url.getParameter("client")  // 选择client
		if clientStr有值
			ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions()
		return server
	}

	/*
	 * ExchangeServer收到请求后，调用getInvoker()获取Invoker执行
	 */
	getInvoker(ExchangeChannel channel, Invocation inv):Invoker<?> throws RemotingException {
		Map<String, String> attachments = inv.getAttachments()
		
		// port
		int port = channel.getLocalAddress().getPort()
		if (isStubServiceInvoke = attachments.get("dubbo.stub.event").equals("true")) == true
			port = channel.getRemoteAddress().getPort()
		
		// path
		String path = attachments.get("path")
		if isClientSide(channel) && isStubServiceInvoke == false
			path += "." + attachments.get("callback.service.instid")
		
		// 找到Exporter，返回它的Invoker
		// <port, path, version, group> 映射出 Invoker
		String serviceKey = serviceKey(port, path, attachments.get("version"), attachments.get("group"))
		DubboExporter<?> exporter = (DubboExporter<?>) super.exporterMap.get(serviceKey)
		return exporter.getInvoker()
	}

	/*
	 * 客户端引用服务
	 */
	refer(Class<T> serviceType, URL url):Invoker<T> throws RpcException {
		return new DubboInvoker<T>(serviceType, url, getClients(url), this.invokers)
	}

字段
	// url地址到ExchangeServer的映射, 导出服务时openServer()处使用
	Map<String, ExchangeServer> serverMap = new ConcurrentHashMap<String, ExchangeServer>()

	// 处理请求
	ExchangeHandler requestHandler = new ExchangeHandlerAdapter() {
		/*
			ChannelHandler 接口的 received() connected() disconnected()
			ExchangeHandler继承了ChannelHandler
		*/
		connected(Channel channel) throws RemotingException {
			Invocation inv = this.createInvocation(channel, channel.getUrl(), "onconnect")
			this.received(channel, inv)
		}
		disconnected(Channel channel) throws RemotingException {
			Invocation inv = this.createInvocation(channel, channel.getUrl(), "disconnect")
			this.received(channel, inv)
		}
		createInvocation(Channel channel, URL url, String methodKey):Invocation {
			String methodName = url.getParameter(methodKey)
			// RpcInvocation构造器: 服务方法名 服务参数类型 服务参数
			RpcInvocation invocation = new RpcInvocation(methodName, new Class<?>[0], new Object[0])
			invocation.setAttachMent("path", "version", "group", "interface")
			return invocation
		}
		received(Channel channel, Object message) throws RemotingException {
			if message != null && message instanceof Invocation
				this.reply(channel, message)
		}
		/*
		 * 响应远程调用请求
		 */
		reply(ExchangeChannel channel, Object message):Object throws RemotingException {
			Invocation invocation = (Invocation) message
			Invoker<?> invoker = getInvoker(channel, invocation) // DubboProtocol::getInvoker()
			RpcContext.getContext().setRemoteAddress(channel.getRemoteAddress())
			return invoker.invoke(invocation)  // 调用
		}
	}