Source interface
	extends LifecycleAware
	extends NamedComponent-(setName/getName)
> setChannelProcessor(ChannelProcessor)
> getChannelProcessor():ChannelProcessor


PollableSource interface
	extends Source
> process():Status  // 由EventDrivenSourceRunner执行时，类似pthread_once
> enum Status {
    READY /*source有event*/, BACKOFF /*source没有event*/
}


BasicSourceSemantics abstract
    // 实现Source接口的基本语义: LifecycleAware接口和Source接口
    // 管理LifecycleState，从而省去子类的管理工作
    implements Source, Configurable-configure(Context)
> 字段
    lifecycleState LifecycleState  // 当前LifecycleAware的状态
    exception      Exception       // start()的异常信息
> 抽象方法
    doConfigure()  doStart()  doStop()
// @Override-Configurable
> configure(Context context)
    setLifecycleState(LifecycleState.IDLE)
    this.doConfigure(context)
// @Override-LifecycleAware
> start()
    try
        doStart()
        this.setLifecycleState(LifecycleState.START)
    catch Exception e
        this.exception = e
        this.setLifecycleState(LifecycleState.ERROR)
> stop()
    this.doStop()
    this.setLifecycleState(LifecycleState.STOP)
> getLifecycleState()
    return this.lifecycleState
// @Override-Source
> setChannelProcessor(ChannelProcessor cp)
> getChannelProcessor()
    关联字段: this.channelProcessor
// @Override-NamedComponent
> setName(String name)
> getName()
    关联字段: this.name


AbstractPollableSource
    extends BasicSourceSemantics
    implements PollableSource
> 抽象方法
    doProcess():PollableSource.Status
> process() @Override-PollableSource
    e = BasicSourceSemantics.getStartException()
    if e != null  // 启动有问题
        throw new FlumeException
    if !BasicSourceSemantics.isStarted()  // 没有调用过Source.start()
        throw new EventDeliveryException
    return this.doProcess()


SequenceGeneratorSource
    extends AbstractPollableSource
> 字段
    sequence       long = 0
    batchSize      int
    batchArrayList List<Event>
    sourceCounter  SourceCounter
> doConfigure(Context context) @Override-BasicSourceSemantics
    batchSize = context.getInteger("batchSize", 1)
    if batchSize > 1
        batchArrayList = new ArrayList<Event>(batchSize)
    totalEvents = context.getLong("totalEvents", Long.MAX_VALUE)
    if sourceCounter == null
        sourceCounter = new SourceCounter(NamedComponent.getName())
> produceEvent():Event
    return EventBuilder.withBody( String.valueOf(sequence++).getBytes() )
> doProcess() @Override-AbstractPollableSource
    status = PollableSource.Status.READY
    if batchSize <= 1
        Source.getChannelProcessor().processEvent( produceEvent() )
        sourceCounter.incrementEventAcceptedCount()
    else
        batchArrayList.clear()
        for i = 0; i < batchSize; i++
            batchArrayList.add(i, produceEvent())
        Source.getChannelProcessor().processEventBatch( batchArrayList )
        sourceCounter.incrementAppendBatchAcceptedCount()
        sourceCounter.addToEventAcceptedCount(batchArrayList.size())
> doStart() @Override-BasicSourceSemantics
    sourceCounter.start()
> doStop() @Override-BasicSourceSemantics
    sourceCounter.stop()