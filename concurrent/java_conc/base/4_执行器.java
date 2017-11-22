创建ThreadPoolExecutor对象的两种方法
> ThreadPoolExecutor-有4个构造器
> Executors-工厂类提供了各种工厂方法

Executors::newCachedThreadPool()
> new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>())
  一分钟内没有被使用过的线程会被销毁(corePoolSize是0)，空闲时间(60L, TimeUnit.SECONDS)
> 创建缓存线程池，实现线程重用
  当有新任务要执行时，先从缓存里取可用线程，如果没有可用线程则创建新线程。线程数没有上限
  适用于任务执行时间短，并发量不大
> executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
  executor.execute(runner);
  executor.shutdown();

Executors::newFixedThreadPool(threadNum)
> new ThreadPoolExecutor(threadNum, threadNum, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>())

Executors::newSingleThreadExecutor()
> Executors.newFixedThreadPool(1)

获取任务的执行结果
ThreadPoolExecutor::submit(Callable<T> call)
> Future<T> future = executor.submit(call)

Future<T>
> get(): T
> get(long timeout, TimeUnit): T
> isDone(): boolean
> cancel(boolean mayInterruptIfRunning): boolean
  返回true，表示取消成功；返回false，表示没有被取消，可能原因是这个任务已经完成了
  mayInterruptIfRunning设置true，表示为了停止任务可以调用线程的interrupt()
> isCancelled(): boolean

执行多个任务并获取所有结果
List<? extends Callable<T>> callers;
List<Future<T>> futures = executor.invokeAll(callers);

执行周期任务
Executors::newScheduledThreadPool(int corePoolSize)
> new ScheduledThreadPoolExecutor(corePoolSize)
    super(corePoolSize, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS, new DelayedWorkQueue())
    ScheduledThreadPoolExecutor extends ThreadPoolExecutor
> ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  ScheduledFuture<T> future = executor.scheduleAtFixedRate(runner, initDelay, period, timeUnit);