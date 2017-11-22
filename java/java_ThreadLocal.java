ThreadLocal<T>
	存放一个线程安全的局部变量
	目的是让每个线程有一个副本, 从而实现线程安全
使用场景
	public class ConnectionManager {
		private static ThreadLocal<Connection> connectionHolder = new ThreadLocal<Connection>() {
			@Override
			protected Connection initialValue() {
				try {
					Class.forName("com.mysql.jdbc.Driver").newInstance();
					Connection conn = DriverManager.getConnection(
						"jdbc:mysql://localhost:3306/test", "username", "password");
					return conn;
				} catch (SQLException e) {
					LOGGER.error("", e);
					return null;
				}
			}
		};  // new ThreadLocal<Connection>
		public static Connection getConnection() {
			return connectionHolder.get();
		}
	}  
方法
	get():T set(T) remove()
实现原理
	每个Thread对象里有一个ThreadLocalMap threadLocals字段
	threadLocals的key是ThreadLocal对象, value是ThreadLocal保存的变量
	用Map的好处是一个Thread对象对应多个局部变量
	使用局部变量时, 客户调用ThreadLocal.get()从threadLocals中用this取出value
	因为每次get()时都访问当前Thread与之关联, 所以是线程安全的


ThreadLocal<T>
内部类
	static class ThreadLocalMap
字段
	- static AtomicInteger nextHashCode = new AtomicInteger()
	- final int threadLocalHashCode = nextHashCode()
方法
	- static int nextHashCode() {
		return nextHashCode.getAndAdd(HASH_INCREMENT)  // HASH_INCREMENT = 0x61c88647
	}
	set(T v) {
		Thread t = Thread.currentThread()
		ThreadLocalMap map = t.threadLocals  // Thread类的字段ThreadLocalMap threadLocals
		if map != null
			map.set(this, v)
		else
			createMap(t, v)
	}
	get():T {
		ThreadLocalMap map = Thread.currentThread().threadLocals
		if map != null
			ThreadLocalMap.Entry e = map.getEntry(this)
			if e != null
				return (T) e.value
		return setInitialValue()
	}
	remove() {
		ThreadLocalMap map = Thread.currentThread().threadLocals
		if map != null
			map.remove(this)
	}
	setInitialValue():T {  // set()方法的另外一种形式
		T v = initialValue()
		set(v)
		return v
	}
	createMap(Thread t, T v) {
		t.threadLocals = new ThreadLocalMap(this, v)
	}

ThreadLocalMap
	并不是用拉链法解决Hash冲突, Knuth 6.4 Algorithm R
内部类
	Entry extends WeakReference<ThreadLocal<?>> {
		- Object value
		Entry(ThreadLocal<?> k, Object v) {
			super(k)
			value = v
		}
	}
字段
	- Entry[] table  // 数组大小始终是2的幂次
	- int size  // table包含的元素数目
方法
	- nextIndex(i, len):int {
		return i+1 < len ? i+1 : 0
	}
	- prevIndex(i, len):int {
		return i-1 >= 0 ? i-1 : 0
	} 
	- set(ThreadLocal<?> k, Object v) {
		len = table.length
		i = k.threadLocalHashCode & (len-1)  // i不会等于len-1, table[len-1]始终null
		for e = table[i]; e != null; e = table[i = nextIndex(i, len)]
			k1 = e.get()  // 调用WeakReference<T>.get():T
			if k1 == k
				e.value = v; return
			if k1 == null
				replaceStableEntry(k, v, i); return
		table[i] = new Entry(k, v)
		sz = ++size
		if !cleanSomeSlots(i, sz) && sz >= threshold
			rehash()
	}
	- getEntry(ThreadLocal<?> k):Entry {
		i = k.threadLocalHashCode & (table.length - 1)
		Entry e = table[i]
		if e != null && e.get() == k
			return e
		else
			return getEntryAfterMiss(k, i, e)
	}
	- getEntryAfterMiss(ThreadLocal<?> k, int i, Entry e):Entry {
		for ; e != null; e = table[i]
			ThreadLocal<?> k1 = e.get()
			if k1 == k
				return e
			if k1 == null
				expungeStableEntry(i)  // expunge(删除)table的第i个元素, table[i]=null
			else
				i = nextIndex(i, table.length)
		return null;
	}
	- expungeStableEntry(int stableSlot):int {
		len = table.length
		// 删除第stableSlot个元素
		table[stableSlot].value = null;
		table[stableSlot] = null;
		size--;
		// 继续往后面查看是否有元素要删除
		for i=nextIndex(stableSlot,len); (e=table[i])!=null; i=nextIndex(i,len)
			ThreadLocal<?> k1 = e.get()
			if k1 == null  // null的Entry元素要被删除
				e.value = null
				table[i] = null
				size--
			else  // 不被删除的元素进行重哈希(rehash)
				h = k1.threadLocalHashCode & (len-1)
				if h != i  // hash冲突引起 h不等于i
					table[i] = null  // 删除第i个元素
					// 重新插入e, h相对i是往前移了(到了hash冲突的第一个位置)
					while table[h] != null
						h = nextIndex(h, len)
					table[h] = e
		return i
	}