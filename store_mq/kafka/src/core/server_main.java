object Kafka extends Logging {
	// 启动的main函数
	def main(args: Array[String]) {
		try {
			// 配置文件server.properties
			val serverProps = this.getPropsFromArgs(args)
								==> val props = Utils.loadProps(args(0))  // args(0)是server.properties
			val kafkaServerStartable = new KafkaServerStartable(new KafkaConfig(serverProps, doLog=true), reporters)

			Runtime.getRuntime.addShutdownHook(new Thread() {
				override def run() {
					kafkaServerStartable.shutdown()
				}
			})

			kafkaServerStartable.startup()
			kafkaServerStartable.awaitShutdown()
		} catch {
			case e: Throwable =>
				super.fatal(e)
				System.exit(1)
		}
		System.exit(0)
	}
}


class KafkaServerStartable(val serverConfig: KafkaConfig, reporters: Seq[KafkaMetricsReporter]) {
	val server = new KafkaServer(serverConfig, kafkaMetricsReporters=reporters)

	def startup() = server.startup()
	def shutdown() = server.shutdown()
	def awaitShutdown() = server.awaitShutdown()
}