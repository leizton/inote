RequestChannel
    // {SocketServer}调用该构造函数
    (numProcessors: Int, queueSize: Int)

> 字段
    requestQueue = new ArrayBlockingQueue[RequestChannel.Request](queueSize)
    // 每个{processor}有一个BlockingQueue的responseQueue
    responseQueues = new Array[ BlockingQueue[RequestChannel.Response] ](numProcessors)

// Processor::processCompletedReceives()调用，添加新的请求
// KafkaRequestHandler::shutdown()调用，添加结束标志请求{RequestChannel.AllDone}，在AllDone后面的请求都不会再被处理
> sendRequest(RequestChannel.Request req)
    this.requestQueue.put(req)

// KafkaRequestHandler::run()调用，取出request通过KafkaApis处理
> receiveRequest(long timeoutMillis):RequestChannel.Request
    return this.requestQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS)

// Processor::processNewResponses()调用
> receiveResponse(int processorId):RequestChannel.Response
    return this.responseQueues[processorId].poll()




RequestChannel.Request
    // Processor::processCompletedReceives()调用该构造函数，在{selector}的poll()完成后.
    (processId, connectionId /* kChannelId */, ByteBuffer buffer /* NetworkReceive的buffer字段 */, ...)

> 字段
    requestId = buffer.getShort(); buffer.rewind()
    header: RequestHeader = new RequestHeader(Protocol.REQUEST_HEADER.read(buffer) /* RequestHeader对应的Struct的实例 */)
    bodyAndSize: RequestAndSize = AbstractRequest.getRequest(header.apiKey, header.apiVersion, buffer)

> body[T <: AbstractRequest](implicit classTag: ClassTag[T], nn: NotNothing[T]):T
    // 对{bodyAndSize}的类型检查
    bodyAndSize.request match {
        case r: T => r
        case r: => throw new CLassCastString("Expected request with type ${classTag.runtimeClass}, but found ${r.getClass}")
    }




/**
 * 数据结构: RequestHeader, RequestAndSize
 */
RequestHeader
> 字段
    short   apiKey, apiVersion
    String  clientId
    int     correlationId


RequestAndSize
> 字段
    AbstractRequest  request
    int              size




/**
 * AbstractRequest::getRequest()-解析出RequestAndSize
 */
AbstractRequest
> getRequest(int apiKeyId, short apiVersion, ByteBuffer buffer):RequestAndSize
    // {ApiKeys}是一个枚举类型
    ApiKeys apiKey = ApiKeys.forId(apiKeyId)

    Struct struct = apiKey.parseRequest(apiVersion, buffer) {
        // 先由apiVersion找到Schema，再解析buffer
        Schema schema = apiKey.schemaFor(Protocol.REQUESTS, apiVersion)
        return schema.read(buffer)
    }

    AbstractRequest request = apiKey match {
        case ApiKeys.PRODUCE => new ProduceRequest(struct, apiVersion)  // @see Protocol.PRODUCE_REQUEST_V3
        case ApiKeys.FETCH   => new FetchRequest(struct, apiVersion)
        ...
    }
    return new ReuqestAndSize(request, struct.sizeOf())