/* 线程同步 */

临界区
	一段访问共享资源的代码块，这段代码块在任意时间最多只有一个线程在执行

synchronized
	> 作用于非static方法时，互斥量(内存局部区域)是this
		public synchronized void addAmount(BigDecimal)  // 转账
		public synchronized void subtractAmount(BigDecimal)  // 取钱
	> 作用于代码块
		synchronized(obj) {
			...
		}

条件量
Object-的用于做条件量的方法: wait() notify() notifyAll()
生产者
	synchronized(this) {
		while queue.size() == maxSize
			this.wait()
		queue.offer(...)
		this.notify()
	}
消费者
	synchronized(this) {
		while queue.isEmpty()
			this.wait()
		queue.poll()
		this.notify()
	}

锁
Lock-接口
ReentrantLock-实现类，可重入
	lock() unlock() tryLock():boolean
	newCondition():Condition

不可重入锁的死锁情形:
	private synchronized void a() {
		b();  // 如果不可重入的, 那么会出现死锁.
		// synchronized是可重入的.
	}
	private synchronized void b() {
	}

读写锁
ReadWriteLock-接口
ReentrantReadWriteLock-实现类
	readLock():Lock   获取读锁
	writeLock():Lock  获取写锁

锁的公平性
ReentrantLock 和 ReentrantReadWriteLock
构造方法参数"fair"，默认false，非公平锁
当有多个线程等待锁时，对于公平锁，选择让等待时间最长的线程获得锁
fair不影响tryLock()方法

用锁和条件量实现生产者消费者模型
Condition consumeCond = lock.newCondition()
Condition productCond = lock.newCondition()
生产者
	lock.lock()
	try {
		while queue.size() == maxSize  // [得到通知后加锁]，检查条件
			productCond.await()  // 释放锁，并等待
		queue.offer(...)         // 已获得锁
		consumeCond.singal()     // 通知消费者
	} catch (...) {
	} finally {
		lock.unlock()  // 记住要在finally里释放锁
	}
消费者
	lock.lock()
	try {
		while queue.isEmpty()    // [得到通知后加锁]，检查条件
			consumeCond.await()  // 释放锁，并等待
		queue.poll()             // 已获得锁
		productCond.singal()     // 通知生产者
	} catch (...) {
	} finally {
		lock.unlock()  // 记住要在finally里释放锁
	}

注意点
> wait()和await()
	wait()是Object类的方法
	await()才是Contidion的方法
	wait()必须在synchronized的代码块里，而await()是在lock()和unlock()之间