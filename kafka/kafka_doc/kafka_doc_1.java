/* 消息队列的几个概念名词 */
"message"  "topic"  "producer"  "consumer" 
"broker"   "partition"
一个kafka服务器就是一个broker, 一个borker可容纳多个topic

/* 消息分区 partition */
partition存放的消息有序, 序号是offset
每个partition至少安装在一台leader服务器上, 可以附加0或多台follower服务器
consumer可以按任意顺序消费message, consumer可以改变message的offset
一个topic可以对应多个partition
producer决定message分配到哪个partition上

/* 消息的2种发送模式 */
"queuing模式": message发给某个consumer
"publish-subscribe模式": message被广播给所有的consumer-group,
    但consumer-group中只有一个consumer会消费message
    consumer-group是一个"logical subscriber"，包含多个consumer使之可伸缩可容错
一个topic的消息发到3个partition时
    consumer-group-01有1个consumer，这个consumer收3个partition的消息
    consumer-group-02有2个consumer，每次随机有一个consumer收2个partition的消息
    consumer-group-02有3个consumer，每个consumer各收一个partition的消息
    consumer-group-02有4个consumer，每次有3个consumer分别收3个partition的消息

/* 消息的消费顺序的无序性 */
server按序转发message, 但message被异步发给consumer, 所以消息到达consumer时是无序的
如果要严格保证message的顺序, 可以固定一个topic, 一个partition, 且每个consumer-group只有一个consumer

/* kafka应用场景 */
website activity tracking
monitoring data
log aggregation
stream processing, Apache Strom & Samza

/* 命令脚本 */
"创建topic"  指定zk地址  topic==test01  partition==1  replica(副本)==1
    $ bin/kafka-topics.sh --create --zookeeper localhost:2181 --topic test01 --replication-fact 1 --partition 1
"列出有哪些topic"  指定zk地址
    $ bin/kafka-topics.sh --list --zookeeper localhost:2181
"发送消息"  指定broker列表
    $ bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test01
"接收消息"  指定zk地址
    $ bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test01 --from-beginning
    --from-beginning 表示消费生产者发出的所有消息
    不加 --from-beginning 时只接收消费者启动后的消息

/* kafka流 */
// WordCountDemo.java
KStreamBuilder builder = new KStreamBuilder();
final String topic = "streams-file-input"; // 主题
KStream<String, String> source = builder.stream(topic); // 流和主题关联
KTable<String, Long> counts = source
        // value是输入的一行(一个消息), 用空白字符分割成List<String>
        .flatMapValues( value -> Arrays.asList(value.split("\\W+")) )
        // 把原始的<k,v>转成<v,v>, v是分割出的单词
        .map( (key, value) -> new KeyValue<>(value, value) )
        // 结果存入"t_counts"表, 这两步可用countByKey("t_counts")代替
        .groupByKey().count("t_counts");