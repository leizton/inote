AbstractBootstrap<B extends AbstractBootstrap<B, C>, C extends Channel>
> 字段
    ChannelFactory<? extends C>  channelFactory  // Channel工厂
    EventLoopGroup group
    ChannelHandler handler
> channel(Class<? extends C> channelClass):B
    this.channelFactory = new ReflectiveChannelFactory<C>(channelClass) {
        ReflectiveChannelFactory implements ChannelFactory
        > newChannel():C
            return channelClass.newInstance()  // 调用Channel的无参构造方法
    }
> handler(ChannelHandler h):B
    this.handler = h
// 服务端绑定地址
> doBind(SocketAddress localAddress)


// 服务端启动器
ServerBootstrap
    extends AbstractBootstrap<ServerBootstrap, ServerChannel>
> 字段
    EventLoopGroup childGroup
    ChannelHandler childHandler
> group(EventLoopGroup parentGroup, EventLoopGroup childGroup):ServerBootstrap
    // parentGroup是Acceptor线程组，childGroup是IO线程组
    super.group(parentGroup)
    this.childGroup = childGroup
> childHandler(ChannelHandler ch)
    this.childHandler = ch


// 客户端启动器
Bootstrap
    extends AbstractBootstrap<Bootstrap, Channel>
// 客户端连接服务器
> connect(String host, int port):ChannelFuture
    // checkPort: port >= 0 || port <= 0xFFFF
    // checkHost: host != null
    remoteAddress = new InetSocketAddress(checkPort(port), checkHost(host))
    this.doResolveAndConnect(remoteAddress, this.config.localAddress())
> doResolveAndConnect(SocketAddress remoteAddress, SocketAddress localAddress)