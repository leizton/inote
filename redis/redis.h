/* redis.h */

redis的数据类型
#define REDIS_STRING 0
#define REDIS_LIST   1
#define REDIS_SET    2
#define REDIS_ZSET   3
#define REDIS_HASH   4

对整数的编码
// 00 | xxxxxx  =>  00开头, 后面跟6 bits, 共1 byte, 存放6 bits的integer.
#define REDIS_RDB_6BITLEN  0
// 01 | xxxxxx xxxxxxxx  =>  01开头, 后面14 bits, 共2 bytes.
#define REDIS_RDB_14BITLEN 1
// 10 | 32 bits  =>  后面32 bits
#define REDIS_RDB_32BITLEN 2
// 11 | xxxxxx  =>  需要结合REDIS_RDB_ENC_*, 表示有符号数
#define REDIS_RDB_ENCVAL   3

client flag
#define REDIS_SLAVE   (1<<0)   // client is slave server
#define REDIS_MASTER  (1<<1)   // client is master server
#define REDIS_MONITOR (1<<2)   // client is slave monitor
#define REDIS_PUBSUB  (1<<18)  // client is in Pub/Sub mode

client 阻塞类型
#define REDIS_BLOCKED_NONE 0
#define REDIS_BLOCKED_LIST 1
#define REDIS_BLOCKED_WAIT 2

client 请求类型
#define REDIS_REQ_INLINE    1
#define REDIS_REQ_MULTIBULK 2

slave replication 复制状态
server.repl_state存放以下状态值, 让slave知道下一步做什么.
#define REDIS_REPL_NONE           0  /* No active replication */
#define REDIS_REPL_CONNECT        1  /* Must connect to master */
#define REDIS_REPL_CONNECTING     2  /* Connecting to master */
// slave与master的握手状态
#define REDIS_REPL_RECEIVE_PONG   3  /* Wait for PING reply */
#define REDIS_REPL_SEND_AUTH      4  /* Send AUTH to master */
#define REDIS_REPL_RECEIVE_AUTH   5  /* Wait for AUTH reply */
#define REDIS_REPL_SEND_PORT      6  /* Send REPLCONF listening-port */
#define REDIS_REPL_RECEIVE_PORT   7  /* Wait for REPLCONF reply */
#define REDIS_REPL_SEND_CAPA      8  /* Send REPLCONF capa */
#define REDIS_REPL_RECEIVE_CAPA   9  /* Wait for REPLCONF reply */
#define REDIS_REPL_SEND_PSYNC     10 /* Send PSYNC */
#define REDIS_REPL_RECEIVE_PSYNC  11 /* Wait for PSYNC reply */
// 握手结束状态
#define REDIS_REPL_TRANSFER       12 /* Receiving .rdb from master */
#define REDIS_REPL_CONNECTED      13 /* Connected to master */

client->replstate存放以下状态值
#define REDIS_REPL_WAIT_BGSAVE_START  14 /* We need to produce a new RDB file. */
#define REDIS_REPL_WAIT_BGSAVE_END    15 /* Waiting RDB file creation to finish. */
#define REDIS_REPL_SEND_BULK          16 /* Sending RDB file to slave. */
#define REDIS_REPL_ONLINE             17 /* RDB file transmitted, sending just updates. */

日志级别
#define REDIS_DEBUG    0
#define REDIS_VERBOSE  1
#define REDIS_NOTICE   2
#define REDIS_WARNING  3
#define REDIS_LOG_RAW  (1<<10) /* Modifier to log without timestamp */
#define REDIS_DEFAULT_VERBOSITY REDIS_NOTICE

当前LRU时钟
#define LRU_CLOCK() \
	((1000/server.hz <= 1000) ? server.lruclock : getLRUClock())

Redis对象, 可以表示string/list/set
typedef struct redisObject {
    unsigned type:4;
    unsigned encoding:4;
    unsigned lru:24;  // LRU时间
    int refcount;
	void *ptr;
} robj;

Redis对象结构体的encoding的取值
#define REDIS_ENCODING_RAW         0 /* Raw representation */
#define REDIS_ENCODING_INT         1 /* Encoded as integer */
#define REDIS_ENCODING_HT          2 /* Encoded as hash table */
#define REDIS_ENCODING_ZIPMAP      3 /* Encoded as zipmap */
#define REDIS_ENCODING_LINKEDLIST  4 /* Encoded as regular linked list */
#define REDIS_ENCODING_ZIPLIST     5 /* Encoded as ziplist */
#define REDIS_ENCODING_INTSET      6 /* Encoded as intset */
#define REDIS_ENCODING_SKIPLIST    7 /* Encoded as skiplist */
#define REDIS_ENCODING_EMBSTR      8 /* Embedded sds string encoding */

Redis对象的初始化
#define initStaticStringObject(_var,_ptr) do { \
    _var.refcount = 1; \
    _var.type = REDIS_STRING; \
    _var.encoding = REDIS_ENCODING_RAW; \
    _var.ptr = _ptr; \
} while(0);

