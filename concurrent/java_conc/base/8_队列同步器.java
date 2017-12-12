AbstractOwnableSynchronizer
// 用于跟踪当前是哪个线程独占了同步器
- exclusiveOwnerThread  Thread  当前独占同步器的线程


AbstractQueuedSynchronizer
// 实现同步阻塞锁和如信号量等同步器的框架