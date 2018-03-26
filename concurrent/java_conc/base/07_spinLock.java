自旋锁的本质都是使用原子引用实现
实现类有：SpinLock，TicketLock，CLHLock，MCSLock

/**
 * 最简单的自旋锁
 */
class SpinLock {
    AtomicReference<Thread> spin

    lock() {
        Thread currentThread = Thread.currentThread()
        // 直到其他线程unlock()把spin设成null，才跳出循环
        while !spin.compareAndSet(null, currentThread);
    }

    unlock() {
        Thread currentThread = Thread.currentThread()
        spin.compareAndSet(currentThread, null)
    }
}

/**
 * 弥补SpinLock不具有访问顺序的缺点, 即把非公平的SpinLock变成公平锁.
 * 缺点是对currentTicket的get/compareAndSet操作影响性能.
 */
class TicketLock {
    static final ThreadLocal<Integer> localTicket = new ThreadLocal();  // 当前线程的票号

    AtomicInteger currentTicket = new AtomicInteger();  // 当前令牌的票号
    AtomicInteger ticketCounter = new AtomicInteger();  // 票号计数器

    lock() {
        int ticket = ticketCounter.getAndIncrement()  // 售票, 原子增长的票号使得访问有序
        localTicket.set(ticket)  // ThreadLocal的set/get开销不小
        // 票号有效时(轮到自己了)跳出循环
        while ticket != currentTicket.get();
    }

    /**
     * 当ticketCounter.get()==Integer.MAX_VALUE时,
     * 执行ticketCounter.getAndIncrement()后, 变成Integer.MIN_VALUE,
     * 不受溢出的影响.
     */

    unlock() {
        int ticket = localTicket.get()
        // 让拿着第ticket+1号票的线程可以加锁成功.
        currentTicket.compareAndSet(ticket, ticket + 1)
    }
}

/**
 * 采用链表形式实现自旋公平锁.
 * CLH: Craig, Landin, and Hagersten
 */
class CLHLock {
    static final class CLHLockNode {
        volatile boolean isEnd = false
    }

	// <对象类型, 字段类型>
    static val UPDATER = new AtomicReferenceFieldUpdater<CLHLock, CLHLockNode>(CLHLock.class, "tail");

    val ThreadLocal threadNode = new ThreadLocal<CLHLockNode>();  // 不能是static
    volatile CLHLockNode tail;

    lock() {
		CLHLockNode node = lockNode.get();
		if node != null
			return;  // 可重入

		node = new CLHLockNode();
		threadNode.set(node);

        // 多个线程一个接一个原子地改变tail, 从而形成链表
        CLHLockNode pred = UPDATER.getAndSet(this, node)
        if pred != null
			// CLHLockNode::isEnd的默认值是false
            while !pred.isEnd {}  // 前一个线程unlock后, 退出循环
    }

    unlock() {
        CLHLockNode node = threadNode.get()
		if node != null
			UPDATER.compareAndSet(this, node, null)
			node.isEnd = true  // 下一个后继线程在lock()中阻塞在while(!pred.isEnd)
			threadNode.set(null)
    }
}