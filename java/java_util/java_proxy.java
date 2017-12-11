一个动态代理的例子:
public class ProxyTest {
    @Test
    public void test() {
        InvocationHandler proxy = new PrintServiceProxy(new PrintServiceImpl());
        PrintService service = (PrintService) Proxy.newProxyInstance(
				this.getClass().getClassLoader(), new Class<?>[] { PrintService.class }, proxy);
        System.out.println(service.hashCode());  // 和下面打印的hashCode()值相同
        service.print("hello");
		/**
		 * 共打印3次hashCode值
		 * 上面调用service.hashCode()触发代理方法PrintServiceProxy::invoke()调用，第一次打印hashCode
		 * 上面的System.out.println()打印第二次hashCode
		 * 上面的service.print("hello")触发第三次打印hashCode
		 */
    }

    // service代理
    private class PrintServiceProxy implements InvocationHandler {
        // 注入service实现
        private Object serviceImpl;
        PrintServiceProxy(Object serviceImpl) {
            this.serviceImpl = serviceImpl;
        }

		@Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println(serviceImpl.hashCode());  // 和上面打印的hashCode()值相同
            return method.invoke(serviceImpl, args);
        }
    }

    // service接口和实现
    private interface PrintService {
        void print(String message);
    }
    private class PrintServiceImpl implements PrintService {
        public void print(String message) {
            System.out.println(message);
        }
    }
}


不同于代理模式
PrintService接口  PrintServiceProxy代理对象  PrintServiceImpl实际对象
@see "design_pattern/代理模式.jpg"


Proxy
/* 依赖关系
+ newProxyInstance()
  + proxyClassCache.get() 获取或创建代理类对象

+ proxyClassCache  缓存
  + KeyFactory  Key的工厂类, 缓存中创建key
    > Key1  1个service接口
	> Key2  2个service接口
	> KeyX  多个service接口
  + ProxyClassFactory  Value的工厂类, 缓存中创建代理对象
*/
内部类
	Key1, Key2
		extends WeakReference<Class<?>>
	KeyX
	KeyFactory  // key工厂
		// BiFunction<param1, param2, returnType> 是有两个形参的函数
		implements BiFunction<ClassLoader, Class<?>[], Object>
	方法
		@Override apply(ClassLoader classLoader, Class<?>[] interfaces):Object {
			switch (interfaces.length) {
				case 1: return new Key1(interfaces[0])  // 该情形出现地最频繁
				case 2: return new Key2(interfaces[0], interfaces[1])
				case 0: return key0  // Proxy.key0 = new Object()
				default: return new KeyX(interfaces)
			}
		}
	ProxyClassFactory  // 代理实例工厂
		implements BiFunction<ClassLoader, Class<?>[], Class<?>>
	方法
		// 这个apply()是jdk动态代理实现的关键代码
		@Override apply(ClassLoader loader, Class<?>[] interfaces):Class<?> {
			Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap<>(interfaces.length)
			/* 对要代理的service接口检查 */
			for Class<?> ci : interfaces
				// 确保类对象可从loader中获取
				Class<?> c = null
				try  c = Class.forName(ci.getName(), false, loader)
				catch ClassNotFoundException  // 吞掉异常
				if c != ci
					throw "不能从loader获取ci"
				// ci必须是接口
				if ! c.isInterface()
					throw "不是接口"
				// 把c放入集合中
				if interfaceSet.put(c, Boolean.TRUE) != null
					throw "interfaces有重复的接口"
			/* 代理类的包名 */
			String proxyPkg = null  // service接口所在的包名
			for Class<?> ci : interfaces
				if ! Modifier.isPublic(ci.getModifiers())
					int n = ci.getName().lastIndexOf('.')
					String pkgName = (n == -1) ? "" : ci.getName().substring(0,n+1)  // ci的包名
					if proxyPkg == null
						proxyPkg = pkgName
					else if ! pkgName.equals(proxyPkg)
						throw "non-public不是在同一个包下"
			if proxyPkg == null  // service接口都是public
				proxyPkg = "com.sun.proxy."
			/* 代理类的类名 */
			long num = nextUniqueNumber.getAndIncrement()
			String proxyName = proxyPkg + "$Proxy" + num
			/* 动态编译生成指定的代理类 */
			byte[] proxyClassFile = ProxyGenerator.generateProxyClass(
					proxyName, interfaces, accessFlags)  // 字节码
			return defineClass0(loader, proxyName, proxyClassFile, 0, proxyClassFile.length);
		}
静态字段
	final WeakCache<ClassLoader, Class<?>[], Class<?>> proxyClassCache
			= new WeakCache<>( new KeyFactory(), new ProxyClassFactory() )  // 注入2个工厂
	final Class<?>[] constructorParams = { InvocationHandler.class }
静态方法
	newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler handler):Object {
		Class<?> cl = getProxyClass0(loader, interfaces)  // 从proxyClassCache中找到或生成代理类
		try
			Constructor<?> cons = cl.getConstructor(constructorParams)
			/* Modifier.isPublic(int)用于判断输入状态编码是否是public
			 * Field[] fs = Foo.class.getDeclaredFields(),
			 * Modifier.isPublic(fs[0])  判断fs[0]是否是public字段. */
			if ! Modifier.isPublic( cl.getModifiers() )  // cl不是"public class"时, 成立.
				AccessController.doPrivileged( new PrivilegedAction<Void>() {
					run():Void {
						cons.setAccessible(true)
						return null
					}
				})
			return cons.newInstance(new Object[] { handler })
	}
	getProxyClass0(ClassLoader loader, Class<?>... interfaces):Class<?> {
		return proxyClassCache.get(loader, interfaces)
	}