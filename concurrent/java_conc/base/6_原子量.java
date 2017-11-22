Unsafe
方法
	/**
	 * CAS (compareAndSet)
	 * 如果对象obj的偏移offset整型字段的值是expect, 则更新成update并返回true, 否则返回false.
	 */
	compareAndSwapInt(Object obj, long offset, int expect, int update):boolean native
	// var1-将改变的对象, var2-字段偏移量, var3-增加值
	getAndAddInt(Object var1, long var2, int var3) {
		int var5
		do {
			var5 = this.getIntVolatile(var1, var2)
		} while !this.compareAndSwapInt(var1, var2, var5, var5 + var3)
		return var5
	}
	// 字段的内存偏移量
	objectFieldOffset(Field var1):long native


AtomicInteger
字段
	- static final Unsafe unsafe = Unsafe.getUnsafe()
	- static final long valueOffset
	- volatile int value;
静态块
	static {
		try
			valueOffset = unsafe.objectFieldOffset( AtomicInteger.class.getDeclaredField("value") )
		catch Exception e
	}
方法
	getAndIncrement():int {
		return unsafe.getAndAddInt(this, valueOffset, 1)
	}
	incrementAndGet():int {
		return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
	}
	getAndAdd(int delta):int {
		return unsafe.getAndAddInt(this, valueOffset, delta)
	}


/**
 * 原子地修改类实例的某个字段
 */
AtomicReferenceFieldUpdater<T, V>
内部类
	AtomicReferenceFieldUpdaterImpl<T,V>
	字段
		- static final Unsafe unsafe = Unsafe.getUnsafe()
		- final long offset
		- Class<T> tclass, Class<V> vclass, Class<?> cclass
	构造器
		AtomicReferenceFieldUpdaterImpl(tclass, vclass, fieldName, caller) {
			final Class<?> fieldClass
			try
				// 获取tclass的Field引用
				Field field = AccessController.doPrivileged(
					new PrivilegedExceptionAction<Field>() {
						+ run():Field throws NoSuchFieldException {
							return tclass.getDeclaredField(fieldName)
						}
					});
				// 确保caller可以访问tclass的field这个字段
				int modifiers = field.getModifiers()
				sun.reflect.misc.ReflectUtil.ensureMemberAccess(caller, tclass, null, modifiers)
				//
				ClassLoader cl = tclass.getClassLoader()
				ClassLoader ccl = caller.getClassLoader()
				if ccl != null && ccl != cl && ( cl == null | !isAncestor(cl, ccl) )
					sun.reflect.misc.ReflectUtil.checkPackageAccess(tclass)
				fieldClass = field.getType()
			catch PrivilegedActionException Exception
				throw new RuntimeException()
			
			if vclass != fieldClass
				throw new ClassCastException()
			if vclass.isPrimitive()
				throw new IllegalArgumentException("Must be reference type")
			if ! Modifier.isVolatile(modifiers)
				throw new IllegalArgumentException("Must be volatile type")
			
			this.tclass = tclass
			this.vclass = vclass != Object.class ? vclass : null
			this.cclass = (Modifier.isProtected(modifiers) && caller != tclass) ? caller : null
            offset = unsafe.objectFieldOffset(field)
		}
	方法
		get(T obj):V {
			if obj == null || obj.getClass() != tclass || cclass != null
				targetCheck(obj)
			return (V) unsafe.getObjectVolatile(obj, offset)
		}
		getAndSet(T obj, V newV):V {
			... updateCheck(obj, newV)
			return (V) unsafe.getAndSetObject(obj, offset, newV)
		}
		compareAndSet(T obj, V expect, V update):boolean {
			... updateCheck(obj, update)
			return unsafe.compareAndSwapObject(obj, offset, expect, update)
		}
		targetCheck(T obj) {
			if ! tclass.isInstance(obj)  // obj == null 或 obj不是tclass及其子类的实例
				throw new ClassCastException()
			if cclass != null && ! cclass.isInstance(obj)
				throw new RuntimeException()
		}
静态方法
	newUpdater(Class<T> tclass, Class<V> vclass, String fieldName) {
		return new AtomicReferenceFieldUpdaterImpl(tclass, vclass, fieldName, Reflection.getCallerClass())
		/* Reflection.getCallerClass()等同于Reflection.getCallerClass(2), jdk1.8抛出异常表示该方法不可用.
		   Reflection.getCallerClass(0)返回Reflection;
		   Reflection.getCallerClass(1)返回第1层函数栈的方法是哪个Class<?>的,
				即[Reflection.getCallerClass所在函数]所在的类;
		   Reflection.getCallerClass(2)返回第2层函数栈的方法是哪个Class<?>的,
				即调用[Reflection.getCallerClass所在函数]的函数所在的类;
		   当大于函数栈数时, 返回null.
		*/
	}
方法
	abstract get(T obj):V
	getAndSet(T obj, V newV):V {
		V oldV
		do
			oldV = get(obj)
		while ! compareAndSet(obj, oldV, newV)
		return oldV
	}
	abstract compareAndSet(T obj, V expect, V update):boolean