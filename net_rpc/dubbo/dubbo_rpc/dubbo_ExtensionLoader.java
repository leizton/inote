ExtensionLoader<T>
	package com.alibaba.dubbo.common.extension

静态字段
	SERVICE_DIRECTORY = "META-INF/services/"
	DUBBO_DIRECTORY = "META-INF/dubbo/"
	DUBBO_INTERNAL_DIRECTORY = DUBBO_DIRECTORY + "internal/"
	ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADER
	ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES

静态方法
	static <T> getExtensionLoader(Class<T> type):ExtensionLoader<T> {
		// type必须是接口, 且有@SPI注解
		// 返回 Class<T> 对应的 ExtensionLoader<T>
		ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADER.get(type)
		if loader == null
			EXTENSION_LOADER.putIfAbsent(type, new ExtensionLoader<T>(type))
			loader = (ExtensionLoader<T>) EXTENSION_LOADER.get(type)
		return loader
	}

字段
	Class<?> cachedAdaptiveClass, type
	Holder<Object> cachedAdaptiveInstance  // Holder是存放Object实例的bean
	Holder<Map<String, Class<?>>> cachedClasses
	ConcurrentMap<String, Holder<Object>> cachedInstances

构造器
	ExtensionLoader(Class<?> type) {
		this.type = type
		this.objectFactory = (type == ExtensionFactory.class) ? null
			: ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension()
	}

方法
/*
+ getAdaptiveExtension
  + createAdaptiveExtension
    + getAdaptiveExtensionClass
      + getExtensionClass
        + loadExtensionClasses
          < loadFile
      + createAdaptiveExtensionClass
	    < createAdaptiveExtensionClassCode 生成AdaptiveExtension类的源代码
*/
	// 获取适应性扩展实例
	getAdaptiveExtension():T {
		Object instance = this.cachedAdaptiveInstance.get() // 缓存
		if instance == null
			if createAdaptiveInstanceError == null // 防止上次失败时重试
				try
					instance = createAdaptiveExtension()
					cachedAdaptiveInstance.set(instance)
		return instance
	}
	createAdaptiveExtension():T {
		// injectExtension(instance) 调用instance的setXXX()方法
		// 注入this.objectFactory.getExtension(XXXtype, XXX)的返回值
		return injectExtension( (T) getAdaptiveExtensionClass().newInstance() )
	}
	getAdaptiveExtensionClass():Class<?> {
		getExtensionClass()
		if cachedAdaptiveClass != null
			return cachedAdaptiveClass
		return cachedAdaptiveClass = createAdaptiveExtensionClass()
	}
	getExtensionClass():Map<String, Class<?>> {
		Map<String, Class<?>> classes = this.cachedClasses.get()
		if classes == null
			synchronized (cachedClasses)
				classes = cachedClasses.get()
				if classes == null // 双null检查
					/*
					  loadExtensionClasses调用
					    loadFile( "DUBBO_INTERNAL_DIRECTORY / DUBBO_DIRECTORY / SERVICE_DIRECTORY" )
					*/
					/*
					  "dubbo/pom.xml"(dubbo模块的pom.xml)里有
					    <resource>
					        META-INF/dubbo/internal/com.alibaba.dubbo.rpc.Protocol
					    </resource>等
					  这样在"META-INF"目录的"DUBBO_INTERNAL_DIRECTORY"里有文件"com.alibaba.dubbo.rpc.Protocol"
					  该文件里指定Protocol的实现类是DubboProtocol
					  查看运行DubboProtocolTest后的target/classes/META-INF
					  grep -rn --include="pom.xml" "META-INF"
					*/
					classes = loadExtensionClasses()
					cachedClasses.set(classes)
		return classes
	}
	createAdaptiveExtensionClass():Class<?> {
		Compiler compiler = ExtensionLoader.getExtensionLoader(Compiler.class).getAdaptiveExtension()
		return compiler.compile(createAdaptiveExtensionClassCode(), ExtensionLoader.class.getClassLoader())
	}
	createAdaptiveExtensionClassCode():String {
		if this.type接口的所有方法上都没有@Adaptive注解
			throw new IllegalArgumentException()
		code += "package this.type.packageName; import ExtensionLoader.class.getName();"
		code += "public class this.type.getSimpleName()$Adaptive implement this.type.getCanonicalName() {"
		...
		// 以com.alibaba.dubbo.rpc.Protocol举例, 其export(Invoker)的代码如下
		// @see dubbo_Protocol$Adaptive.java
		public com.alibaba.dubbo.rpc.Exporter // export()返回类型
			// arg0是服务实现的代理对象, @see dubbo_DubboProtocolTest.java
			export(com.alibaba.dubbo.rpc.Invoker arg0) throws com.alibaba.dubbo.rpc.RpcException {
			if (arg0 == null) throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument == null");
			if (arg0.getUrl() == null) throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument getUrl() == null");
			com.alibaba.dubbo.common.URL url = arg0.getUrl();
			String extName = ( url.getProtocol() == null ? "dubbo" : url.getProtocol() );
			if(extName == null) throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.rpc.Protocol) name from url(" + url.toString() + ") use keys([protocol])");
			/*
			  实际提供export(Invoker)的对象是从ExtensionLoader的getExtension()获得
			  com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol
			*/
			com.alibaba.dubbo.rpc.Protocol extension = (com.alibaba.dubbo.rpc.Protocol)
					ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.rpc.Protocol.class).getExtension(extName);
			return extension.export(arg0);
		}
	}
/*
+ getExtension  // 返回指定名字的Extension, cachedInstances
  + createExtension  // EXTENSION_INSTANCES
    + getExtensionClasses  // cachedClasses
	  + loadExtensionClasses
        < loadFile
*/
	getExtension(String name):T {
		if cachedInstances里没有
			createExtension(name)
	}