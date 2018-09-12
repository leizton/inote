DubboProtocolTest
	package com.alibaba.dubbo.rpc.protocol.dubbo
字段
	Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension()
	ProxyFactory proxy = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension()
测试
	testDemoProtocol() {
		// 导出服务
		protocol.export(
			proxy.getInvoker(  // 代理把 具体的用户接口和用户实现 转成 抽象的Invoker
				new DemoServiceImpl(), DemoService.class, // 指定 服务接口DemoService 服务实现DemoServiceImpl
				URL.valueOf("dubbo://127.0.0.1:9020/" + DemoService.class.getName() + "?codec=exchange")
		))
		// 引用服务
		DemoService service = proxy.getProxy(  // 代理把 抽象的Invoker 转成 具体的用户接口和用户实现
			protocol.refer(
				DemoService.class,
				URL.valueOf("dubbo://127.0.0.1:9020/" + DemoService.class.getName() + "?codec=exchange")
		))
		// 调用服务
		assertEquals(service.getSize(new String[]{"", "", ""}), 3)
	}