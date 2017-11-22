Application
// 启动类
> main()
	// 用eventBus向Application通知配置变化
	eventBus = new EventBus(agentName + "-event-bus")
	if isZkConfigured
		configProvider = new PollingZooKeeperConfigurationProvider(
			agentName, zkConnectionStr, baseZkPath, eventBus)
	else
		configProvider = new PollingPropertiesFileConfigurationProvider(
			agentName, file/*配置文件*/, eventBus, interval)
	application = new Application( Lists.newArrayList(configProvider) /* components字段 */ )
	eventBus.register(application)
	application.start()
> start()
	for LifecycleAware component : components
		supervisor.supervise(
			component,
			new SupervisorPolicy.AlwaysRestartPolicy(),
			LifecycleState.START)
> handleConfigurationEvent(MaterializedConfiguration conf) @Subscribe
	// eventBus的订阅函数
	stopAllComponents()
	startAllComponents(conf)
> startAllComponents(MaterializedConfiguration conf)