/* HDFS */
HDFS: Hadoop分布式文件系统
只能追加或删除文件，不能修改文件中的数据，但HBase可以更新写入的数据。
和其他文件系统一样把文件写入多个独立的块中，
	但HDFS的块更大(128MB~512MB)，目的是存储大文件。
	复制因子可配置(默认3)，文件被复制到不同机架(服务器)上。
有2种服务器: NameNode DataNode
	NameNode 负责存储文件系统中文件和块的元数据。
			 元数据包括每个文件映射到块的信息，块的位置信息(所在的DataNode和DataNode上的存储位置)。
	DataNode 负责存储数据。
	任意时刻，存在一个活动NameNode和备用NameNode。
写入过程
	用户提交的数据先写到客户端本地文件；
	客户端刷新文件(用户调flush)，临时文件超过块边界，关闭文件时
	NameNode-为文件分配块，数据写入每个块并复制到各个DataNode上，复制完成后写入操作才成功。
shell命令
	格式: hdfs dfs -command options
	如:   hdfs dfs -ls /Data
文件格式用二进制比文本更好。
	Hadoop-的常用二进制格式是Avro，可拆分，可检测文件中损坏或不完整的部分，支持压缩。

/* flume目的 */
当有大量机器的数据需要收集(存到HDFS)时，如果让每个机器独立地写HDFS可能会有严重的延迟，
  因为被写机器的负载很大。而且随着机器的增多，延迟和失败概率会增加。
flume把HDFS和生产环境的机器隔离开来，让数据以可控有良好组织的方式推送到HDFS等存储系统上。
flume是一个比让应用机器直接写的更好方案，这个方案不保证理想的实时存储，只是让性能更好(延迟更小)。
方案思想: 流式agent提供缓冲和实时数据传输的功能。

/* flume的基本概念/结构/流程 */
agent是最小的部署单元
	agent之间可以组成流式链条，一个agent可接收多个agent的数据。
	多层多个agent使得缓冲容量增大。
	agent的3个组件: Source源  Sink汇  Channel-缓冲source来的数据。
	source写数据到一个或多个channel，一个或多个sink从某个channel读取数据。
	多个source可以安全地写入同一个channel。
事件，source和sink以事件为单位写入读取数据
	事件由报头map和主体组成。报头map由kv pairs组成。
source写流程
"source"              "channel处理器"                       "拦截器"
   |    --1 写事件-->        |         --2 传递-->              |
                            |         <--返回处理后的事件---     |
                            |
                            |                            "channel选择器"
                            |         --3 传递-->              |
                            |         <--返回channel---        |
                            |
                            |                              "channel"
                            |         --4 写到channel-->       |

/* agent配置 */
agent_name.component_type(组件类型，取值sources/sinks/channels).component_name.parameter_name = value
如: 给agent1配2个source，4个sink，2个sink组，2个channel
// 结构
agent1.sources = source1 source2
agent1.sinks = sink1 sink2
agent1.sinkgroups = sg1 sg2
agent1.channels = channel1 channel2s
// sources的参数
agent1.sources.source1.channels = channel1 channel2s
agent1.sources.source1.type = avro
agent1.sources.source1.port = 4144
agent1.sources.source1.bind = avro.domain.com
// sources的channel处理器
agent1.sources.source1.handlers = com.whiker.learn.flume.handler.XXX_Handler
agent1.sources.source1.handlers.insertTimestamp = true
// sources的拦截器
agent1.sources.source1.interceptors = hostInterceptor
agent1.sources.source1.interceptors.hostInterceptor.type = host
// channel类型，内存channel
agent1.channels.channel1.type = memory
agent1.channels.channel2.type = memory

/* source的配置要求 */
1. 至少有一个Channel连接这个source
2. 必须定义source的type
3. 在agent的sources列表里面存在

/* flume自带的2种channel */
Memory 和 File
JVM-或机器重启, Memory-会丢数据, File-不会。

/* 事务 */
flume事务指channel里同一批写入或删除的事件。
source和sink在写入读取某个channel时，会让这个channel启动一个事务。
事务在source写事件到channel时
	当事件成功写入到每个channel后，channelHandler才提交事务，否则回滚并关闭这个事务。
	所以不会出现source写多个channel时，某些channel的事件写入成功而某些失败。
事务在sink输出事件到存储系统时
	当事件在最终目的地是安全时，事务才被提交，然后channel可以删除本次事务中的事件。
过程
	1. source利用channel成功提交事务后，发一个ACK给sink，表明事件在接收事件agent的channel中是安全的
	2. sink收到ACK后，从channel中读，存储成功后提交事务，表明事件可以在channel中删除

/* 批量大小 */
批量大小指一次事务中的事件数目。
批量过小，会使文件同步的系统调用、RPC-调用等的发生次数过多，开销过大；
批量过大，会增加事件重复的风险，因为每一批的失败导致大量事件需要再次写入。

/* 几类source */
Avro source使用netty服务器来处理请求。
Thrift source是一个thrift服务器。
Http source是一个web服务器，接收post请求。

/* flume事件 */
interface Event {
	getHeaders():Map<String, String>
	getBody():byte[]
	setHeaders(Map<String, String>)
	setBody(byte[])
}

/* RPC客户端接口 */
interface RpcClient {
	append(Event)
	appendBatch(List<Event>)
	isActive():boolean  // 检查RPC客户端是否可执行
	getBatchSize():int
	close()
}
RpcClient client = RpcFactory.getDefaultInstance(host, post, batchSize);

/* Thrift RPC客户端 */
struct ThriftFlumeEvent {
	1: required map<string, string> headers,
	2: required binary body
}
enum Status {
	OK, FAILED, ERROR, UNKNOW
}
service ThriftSourceProtocol {
	Status append(1: ThriftFlumeEvent event),
	Status appendBatch(1: list<ThriftFlumeEvent> events)
}

设无法访问存储系统的最大时间是MaxSTR，高峰单位时间有Pmax个事件，
所以整体channel容量是"1.25 * Pmax * MaxSTR"，浮动25%。