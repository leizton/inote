一个动态代理的例子:
数据库连接的动态代理
	class ConnectionHandler implements InvocationHandler {
		invode(Object proxy, Method method, Object[] args):Object throws Throwable {
			if method.getName.euqals("commit")
				...  // 调用Connection实现对象的commit()
		}
	}
	// Connection是一个接口
	// jdk
	Connection conn = (Connection) Proxy.newProxyInstance(Foo.class.getClassLoader(),
			new Class<?>[] { Connection }, new ConnectionHandler())
	// guava
	Connection conn = Reflection.newProxy(Connection, new ConnectionHandler())


Reflection
静态方法:
	// 创建动态代理对象, 使用上比jdk的Proxy.newProxyInstance()更简单.
	newProxy(Class<T> interfaceType, InvocationHandler handler):T {
		/* Class.isInterface()是native方法.
		 * String.class.isInterface(): false
		 * Map.class.isInterface(): true */
		checkArgument: interfaceType.isInterface()
		/* InvocationHandler接口: 
		 * invode(Object proxy, Method m, Object[] args):Object throws Throwable */
		checkNotNull: handler  // handler客户端定义的代理对象的回调方法
		
		Object proxy = Proxy.newProxyInstance(interfaceType.getClassLoader(),
				new Class<?>[] { interfaceType }, handler)
		return interfaceType.cast(proxy)
	}