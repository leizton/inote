Exchangers
	package com.alibaba.dubbo.remoting.exchange
静态方法
	// Exchanger::bind 返回 ExchangeServer
	bind(URL url, ExchangeHandler handler):ExchangeServer {
		String type = url.getParameter("exchanger", "header")  // default=header
		// META-INF 指定 HeaderExchanger 实现 Exchanger
		Exchanger ec = ExtensionLoader.getExtensionLoader(Exchanger.class).getExtension(type)
		return ec.bind(url, handler)
	}


// Exchanger 由 HeaderExchanger实现
Exchanger
	@SPI("header")  // HeaderExchanger.NAME，该注解用于把 HeaderExchanger 写到 META-INF
接口
	@Adaptive( { "exchanger" } )  // 服务端
	bind(URL url, ExchangeHandler handler):ExchangeServer
	@Adaptive( { "exchanger" } )  // 客户端
	connect(URL url, ExchangeHandler handler):ExchangeClient


// HeaderExchanger
HeaderExchanger
	package com.alibaba.dubbo.remoting.exchange.support.header
	implements Exchanger

静态字段
	String NAME = "header"

实现方法
	// 绑定，服务端
	bind(URL url, ExchangeHandler handler):ExchangeServer {
		Server s = Transporters.bind(url, handler)  // @see dubbo_transports
		return new HeaderExchangerServer(s)  // @see dubbo_ExchangeServer
	}
	// 连接，客户端
	connect(URL url, ExchangeHandler handler):ExchangeClient {
		Client c = Transporters.connect(url, handler)  // @see dubbo_transports
		return new HeaderExchangeClient(c)
	}