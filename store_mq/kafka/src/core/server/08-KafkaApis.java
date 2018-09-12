/**
 * 在{KafkaServer::startup()}里创建，并传给{KafkaRequestHandlerPool}
 */
KafkaApis (
    RequestChannel  requestChannel,
    ReplicaManager  replicaManager,
    ...
)

> handle(RequestChannel.Request request)
    ApiKeys.forId(request.requestId) match {  // dispatch
        case ApiKeys.PRODUCE => handleProducerRequest(request)
        ...
    }

> handleProducerRequest(RequestChannel.Request request)
    ProduceRequest produceRequest = request.body[ProduceRequest]

    /**
     * TopicPartition: { int partition, String topic }
     */

     /**
      * produceRequest.partitionRecords: Map<TopicPartition, MemoryRecords>
      * 把{produceRequest.partitionRecords}根据表达式是true还是false分成2部分
      */
    val (existAndAuthorizedTopicPartition, nonExistAndAuthorizedTopicPartition) =
        produceRequest.partitionRecords.asScala.partition { case (topicPartition, _) =>
            topic = topicPartition.topic
            this.authorize(produceRequest.session, Operation.Describe, new Resource(topic)) && this.metadataCache.contains(topic)
        }

    /**
     * 经过2步过滤后，得到{authorizedRequestInfo}
     */
    val (authorizedRequestInfo, unauthorizedForWriteRequestInfo) =
        existAndAuthorizedTopicPartition.partition { case (topicPartition, _) =>
            this.authorize(produceRequest.session, Operation.Write, new Resource(topicPartition.topic))
        }

    def sendResponseCallback(responseStatus: Map[TopicPartition, PartitionResponse]) {
        // mapValues()对map的每个value做转换
        val mergedResponseStatus = responseStatus ++
            nonExistingOrUnauthorizedForDescribeTopics.mapValues(_ => new PartitionResponse(Errors.UNKNOWN_TOPIC_OR_PARTITION)) ++
            unauthorizedForWriteRequestInfo.mapValues(_ => new PartitionResponse(Errors.TOPIC_AUTHORIZATION_FAILED))

        def produceResponseCallback(delayTimeMs: Int) {
            if produceRequest.acks != 0
                // 返回没有发出去的topic
                val respBody = new ProduceResponse(mergedResponseStatus.asJava, delayTimeMs)
                this.requestChannel.sendResponse(new RequestChannel.Response(request, respBody))
            else
                ...
        }
    }

    /**
     * this.replicaManager.appendRecords()是关键
     */
    if authorizedRequestInfo.isEmpty
        sendResponseCallback(Map.empty)
    else
        this.replicaManager.appendRecords(
            produceRequest.timeout.toLong,
            produceRequest.acks,
            internalTopicsAllowed = request.header.clientId == AdminUtils.AdminClientId,
            authorizedRequestInfo,
            sendResponseCallback
        )
        produceRequest.partitionRecords = null