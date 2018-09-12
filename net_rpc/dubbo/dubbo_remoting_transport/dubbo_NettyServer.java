NettyServer
	extends AbstractServer

构造方法
	NettyServer(URL url, ChannelHandler handler) {
		super(url, handler)  // 调用doOpen()
	}

实现的抽象方法
	doOpen() {
		// 线程池，netty的ServerSocketChannelFactory
		ExecutorService boss = Executors.newCachedThreadPool(new NamedThreadFactory("NettyServerBoss", true))
		ExecutorService worker = Executors.newCachedThreadPool(new NamedThreadFactory("NettyServerWorker", true))
		int workerNum = Runtime.getRuntime().availableProcessors() + 1
		ChannelFactory channelFactory = new NioServerSocketChannelFactory(boss, worker, workerNum)

		// TransportCodec(FastJson Dubbo的json Java序列化)  ThriftCodec  DubboCodec(dubbo自定义格式)
		Codec2 codec = this.getCodec()
		// 解码，编码，Handler
		ChannelHandler decoderHandler = createDecoderHandler(codec)  // encode()事件，codec.decode(channel, msgBuffer)
		ChannelHandler encoderHandler = createEncoderHandler(codec)  // messageReceived()事件，codec.encode(channel, buffer, msg)

		// ServerBootstrap
		this.bootstrap = new ServerBootstrap(channelFactory)
		this.bootstrap.setPipelineFactory(() -> {
			ChannelPipeline pipeline = org.jboss.netty.channel.Channels.pipeline()
			pipeline.addLast("decoder", decoderHandler)
			pipeline.addLast("encoder", encoderHandler)
			pipeline.addLast("handler", NettyServer.this)  // 引用外部类的this
			return pipeline
		})
		this.bootstrap.bind(this.bindAddress)
	}