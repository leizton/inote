import java.util.concurrent.*;

/**
 * 串行互斥执行器
 * 任一时间最多有一个任务在执行
 * 当某个任务已经在执行时，向线程池提交任务会被丢弃
 *
 * @author whiker@163.com create on 16-4-26.
 */
public class SerialMutexExecutor {

    /**
     * 创建没有rejected处理的串行互斥执行器
     */
    public static ThreadPoolExecutor newExecutorNoRejectedHandler() {
        return new NoRejectedHandlerExecutor(new ArrayBlockingQueue<Runnable>(1, true));
    }

    /**
     * afterExecute()方法里清空Runnable队列 <br/>
     * tip: 由于ThreadPoolExecutor.runWorker()的task.run()和afterExecute()之间的缝隙, 所以可能导致类似活锁问题
     */
    private static class NoRejectedHandlerExecutor extends ThreadPoolExecutor {
        private BlockingQueue<Runnable> workQueue; // 任务阻塞队列

        NoRejectedHandlerExecutor(BlockingQueue<Runnable> workQueue) {
            super(1, 1, 0L, TimeUnit.MILLISECONDS, workQueue, new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                }
            });
            this.workQueue = workQueue;
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            workQueue.clear();
        }
    }
}