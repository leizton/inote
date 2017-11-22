import java.lang.Runtime;

Runtime.getRuntime().addShutdownHook(Thread hook) {
	...
	ApplicationShutdownHooks.add(hook)
}

在jvm退出(System.exit, kill命令中断)时, hook可以作清理工作.