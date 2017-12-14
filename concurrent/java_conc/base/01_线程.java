线程状态
	new runnable blocked waiting time-waiting terminated;

Runnable
	::run()

TimeUnit.SECONDS.sleep(1)
	Thread::sleep()由TimeUnit实现

Thread
	start() isInterrupted() setDaemon(boolean)
	join(long millSeconds)

ThreadLocal
	get set remove

ThreadFactory 接口
	::newThread(Runnable r):Thread