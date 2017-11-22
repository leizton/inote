LifecycleState enum
// 生命周期中的各个状态
> IDLE, START, STOP, ERROR

LifecycleAware interface
> start()
> stop()
> getLifecycleState():LifecycleState