信号量
内部有一个计数器，计数器值表示可用的资源数目
默认是非公平的

Semaphore sem = new Semaphore(1)  // 初始计数器是1
try {
    sem.acquire()  // if 计数器==0, wait; else 计数器减1
} catch (InterruptedException) {
} finally {
    sem.release()  // 计数器加1
}

CountDownLatch
> 不是用于保护临界区(lock)或共享资源(sem)，而是用于让多个线程达到同步
  当计数器变成0时，唤醒所有调用它的await()方法的线程
> new CountDownLatch(number)
  countDown()方法使计数器减1
  countDownLatch是一次性的。当计数器变0后，该countDownLatch不可用，必须新建一个

CyclicBarrier
> 和CountDownLatch类似，用于同步threadNum个线程
  但可以指定一个runner(实现了Runnable接口的对象)，在所有线程到达同步点后执行runner的run()方法
> new CyclicBarrier(threadNum, runner)
> cyclicBarrier.await()  // 当前线程在此处等待其他线程到达同步点

Phaser
> 实现并发线程的多阶段(多点)同步。在每一步的结束位置同步所有线程
> new Phaser(threadNum)  // 所有参与同步的线程数
  phaser.arriveAndAwaitAdvance()  // 当前线程已完成当前阶段，等待所有线程完成这个阶段后继续下一个阶段
  phaser.arriveAndDeregister()    // 当前线程已结束当前阶段，并且不进行后续阶段。后面阶段的同步不包括该线程
> phaser对象的2个状态：Termination(所有参与同步的线程都取消注册时)，Active(存在参与同步的线程时)

Exchanger
只能同步2个线程，在同步点处两者交换数据
使用场景：如一个生产者和一个消费者
Exchanger<T> exchanger;
T data;
data = exchanger.exchange(data)