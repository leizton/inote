ReplicaManager

> appendRecords(timeout: Long, requiredAcks: Short, internalTopicsAllowed: Boolean,
				entriesPerPartition: Map[TopicPartition, MemoryRecords],
				responseCallback: Map[TopicPartition, PartitionResponse] => Unit  /* KafkaApis::handleProducerRequest()定义的sendResponseCallback */
	)
	val localProduceResults = this.appendToLocalLog(internalTopicsAllowed, entriesPerPartition, requiredAcks)
	if delayedProduceRequestRequired(requiredAcks, entriesPerPartition, localProduceResults)
		// 延迟生产
		...
	else
		responseCallback(...)

> appendToLocalLog(internalTopicsAllowed: Boolean,
				   entriesPerPartition: Map[TopicPartition, MemoryRecords],
				   requiredAcks: Short
	) : Map[TopicPartition, LogAppendResult]
	entriesPerPartition.map { case (topicPartition, records) =>
		val partition = this.allPartitions.get(topicPartition)
		if partition == null
			throw new UnknownTopicOrPartitionException

		val info = partition.appendRecordsToLeader(records, requiredAcks)

		(topicPartition, LogAppendResult(info))
	}