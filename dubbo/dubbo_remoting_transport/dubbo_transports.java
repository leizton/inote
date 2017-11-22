Transporters
/*
 * 实现类有3种
 * NettyTransporter  MinaTransporter  GrizzlyTransporter
 */
静态方法
	bind(URL url, ChannelHandler handler):Server {
		return new NettyServer(url, handler)  // @see dubbo_NettyServer
	}
	connect(URL url, ChannelHandler handler):Client {
		return new NettyClient(url, handler)  // @see dubbo_NettyClient
	}