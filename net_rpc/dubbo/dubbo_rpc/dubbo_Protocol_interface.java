Protocol
	package com.alibaba.dubbo.rpc
接口
	// export()传入的Invoker由框架或开发者实现并传入，协议不需要关心
	// Exporter用于取消服务暴露
	export(Invoker<T> invoker):Exporter<T>

	// refer()返回的Invoker由协议实现，协议通常需要在此Invoker中发送远程请求
	// type: 暴露的服务接口。url: 服务地址
	refer(Class<T> type, URL url):Invoker<T>

	destroy()               // 释放export和refer占用的资源
	getDefaultPort():int    // 用户未配置端口时用的默认端口


Invoker
接口
	invoke(Invocation invocation):Result    // throws RpcException
	getInterface():Class<T>                 // service interface


AbstractProtocol
	实现接口destory()  // 此处dubbo有内存泄露
字段
	// url到exporter的映射, @see dubbo_DubboProtocol.java#export()
	Map<String, Exporter<?>> exporterMap = new ConcurrentHashMap<String, Exporter<?>>()


Exporter
接口
	getInvoker():Invoker<T>
	unexport()  // getInvoker.destroy()