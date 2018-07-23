KafkaServer
	(config: KafkaConfig, time: Time=Time.SYSTEM, threadNamePrefix: Option[String]=None, kafkaMetricsReporters: Seq[KafkaMetricsReporter]=List())

> 字段
	AtomicBoolean   startupComplete = false
	AtomicBoolean   isStartingUp = false
	KafkaScheduler  kafkaScheduler

> startup()
	if startupComplete
		return  // 已经启动了
	if !isStartingUp.compareAndSet(false, true)
		return  // 有线程正在启动
	brokerState.newState(Starting)
	kafkaScheduler.startup()  // replicaManager里用到
	zkUtils = initZk()
	//
	_clusterId = getOrGenerateClusterId(zkUtils)  // 会在zk上创建持久节点
	config.brokerId = getBrokerId()
	// 网络通信-{SocketServer}
	socketServer = new SocketServer(config, metrics, time, credentialProvider)
	socketServer.startup()
	//
	replicaManager = new ReplicaManager(config, metrics, time, zkUtils, kafkaScheduler, logManager, isShuttingDown, quotaManagers.follower)
	replicaManager.startup()
	//
	kafkaController = new KafkaController(config, zkUtils, brokerState, time, metrics, threadNamePrefix)
	kafkaController.startup()
	//
	groupCoordinator = GroupCoordinator(config, zkUtils, replicaManager, Time.SYSTEM)
	groupCoordinator.startup()
	// 处理请求-{KafkaApis}
	apis = new KafkaApis(this.socketServer.requestChannel, replicaManager, adminManager, groupCoordinator, ...)
	requestHandlerPool = new KafkaRequestHandlerPool(config.brokerId, socketServer.requestChannel, this.apis, config.numIoThreads)




// 封装了一下ScheduledThreadPoolExecutor
KafkaScheduler
> 字段
	ScheduledThreadPoolExecutor executor
> startup() synchronized
	if executor != null
		return
	executor = new ScheduledThreadPoolExecutor(threads)
	// shutdown后不再执行
	executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false)
	executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false)
> schedule(name: String, fun: ()=>Unit, delay: Long, period: Long, unit: TimeUnit) synchronized
	if executor == null
		throw new IllegalStateException
	if period >= 0
		executor.scheduleAtFixedRate(fun, delay, period, unit)
	else
		executor.schedule(fun, delay, unit)