自旋锁的本质都是使用原子引用实现
实现类有：SpinLock，TicketLock，CLHLock，MCSLock


/**
 * 最简单的自旋锁
 */
class SpinLock {
	- AtomicReference<Thread> spin
	
	void lock() {
		Thread currentThread = Thread.currentThread()
		// 直到其他线程unlock()把spin设成null，才跳出循环
		while !spin.compareAndSet(null, currentThread);
	}
	
	void unlock() {
		Thread currentThread = Thread.currentThread()
		spin.compareAndSet(currentThread, null)
	}
}


/**
 * 弥补SpinLock不具有访问顺序的缺点, 即把非公平的SpinLock变成公平锁.
 * 缺点是对currentTicket的get/compareAndSet操作影响性能.
 */
class TicketLock {
	- AtomicInteger currentTicket = new AtomicInteger();  // 当前令牌的票号
	- AtomicInteger ticketCounter = new AtomicInteger();  // 票号计数器
	- static final ThreadLocal<Integer> localTicket = new ThreadLocal();  // 当前线程的票号
	
	void lock() {
		int ticket = ticketCounter.getAndIncrement()  // 售票
		localTicket.set(ticket)  // ThreadLocal的set/get开销不小
		// 票号有效时跳出循环, 或者说轮到自己时跳出.
		while ticket != currentTicket.get();
	}
	
	/**
	 * 当ticketCounter.get()==Integer.MAX_VALUE时,
	 * 执行ticketCounter.getAndIncrement()后, 变成Integer.MIN_VALUE,
	 * 溢出也没有关系.
	 */
	
	unlock() {
		int ticket = localTicket.get()
		/* 让拿着第ticket+1号票的线程加锁成功.
		 * 其实等效于validTicket.getAndIncrement() */
		currentTicket.compareAndSet(ticket, ticket + 1)
	}
}


/**
 * 采用链表形式实现自旋公平锁.
 */
class CLHLock {
	static class CLHLockNode {
		- volatile boolean isLocked = true
	}
	
	- volatile CLHLockNode tail
	- static final ThreadLocal<CLHLockNode> LOCAL = new ThreadLocal()
	- static final AtomicReferenceFieldUpdater<CLHLock, CLHLockNode> UPDATER = new (CLHLock.class, "tail")
	
	lock() {
		CLHLockNode node = new CLHLockNode()
		LOCAL.set(node)
		
		// 多个线程一个接一个原子地改变tail, 形成链表
		// tail存放最近来加锁的线程的node
		CLHLockNode prevNode = UPDATER.getAndSet(this, node)
		
		if prevNode != null
			while prevNode.isLocked;  // 前一个线程unlock后, 退出循环
			prevNode = null
	}
	
	unlock() {
		CLHLockNode node = LOCAL.get(node)
		if ! UPDATER.compareAndSet(this, node, null)
			/**
			 * tail改变了, 说明有其他线程调用了UPDATER.getAndSet(),
			 * 别的线程在lock()中阻塞在 while prevNode.isLocked;
			 */
			node.isLocked = false
		node = null
	}
}