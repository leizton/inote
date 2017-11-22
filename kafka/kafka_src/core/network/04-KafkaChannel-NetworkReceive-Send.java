KafkaChannel
> 字段
	String          id       // channel id
	NetworkReceive  receive
	Send            send
	TransportLayer  transportLayer
	Authenticator   authenticator

> read():NetworkReceive throws IOException
	if receive == null
		receive = new NetworkReceive(maxReceiveSize, this.id)
	receive.readFrom(transportLayer)  // 从传输层读取数据
	NetworkReceive ret = null
	if receive.complete()  // 读到一个完整的NetworkReceive时，返回值才不是null
		receive.payload().rewind()
		ret = receive
		receive = null
	return ret

> mute()
	// 移除{OP_READ}事件，取消读
	if !disconnected
		transportLayer.removeInterestOps(SelectionKey.OP_READ)
	mute = true
> unmute()
	// 添加{OP_READ}事件，恢复读
	if !disconnected
		transportLayer.addInterestOps(SelectionKey.OP_READ)
	mute = false
> isMute():boolean
	return mute

> setSend(Send send)
	// 添加{OP_WRITE}事件
	this.send = send
	transportLayer.addInterestOps(SelectionKey.OP_WRITE)
// write() 被 Selector::pollSelectionKeys() 调用
> write():Send throws IOException
	Send ret
	if this.send != null && send(send)
		ret = send
		this.send = null
	return ret
// private send() 只会被 write() 调用
> send(Send send):boolean throws IOException
	send.writeTo(transportLayer)
	if send.completed()
		transportLayer.removeInterestOps(SelectionKey.OP_WRITE)
		return true
	return false




NetworkReceive
> 字段
	String      source   // kafkaChannel id
	int         maxSize
	ByteBuffer  size = ByteBuffer.allocate(4)  // buffer大小
	ByteBuffer  buffer
/**
 * ByteBuffer.allocate(cap): position=0; limit=capacity=cap; mark=-1
 * hasRemaining(): position<limit  是否有剩余空间用来写数据
 * rewind(): position=0; mark=-1   重置position，从写模式改成读模式
 */
/**
 * KafkaChannel::read()调用readFrom()读取数据
 */
> readFrom(ReadableByteChannel channel):long throws IOException
	int readNum = 0
	if size.hasRemaining()
		// 先读包头的size
		int n = channel.read(size)  // 从position处往后写
		if n < 0: throw new EOFException
		readNum += n
		if !size.hasRemaining()  // position >= limit
			// 已经读取到完整的size
			size.rewind()
			int receiveSize = size.getInt()  // 会使position增加4
			if receiveSize < 0 || (maxSize != -1 && receiveSize > maxSize)
				throw new InvalidReceiveException
			buffer = ByteBuffer.allocate(receiveSize)
	if buffer != null
		// 已经读取到size
		int n = channel.read(buffer); if n < 0: throw new EOFException
		readNum += n
	return readNum
> complete():boolean
	return !size.hasRemaining() && !buffer.hasRemaining()  // size和buffer都写满了




Send
> 接口
	String destination()  // kafkaChannel id
	boolean completed()
	long writeTo(GatheringByteChannel channel) throws IOException
	long size()