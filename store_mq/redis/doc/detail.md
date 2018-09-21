# dict
```c
// 拉链法解决 hash collision
typedef struct dictht {
    dictEntry **table;  //= 每个元素是一个hash桶(dictEntry*)
    unsigned long size;
    unsigned long sizemask;
    unsigned long used;  //= 元素个数
} dictht;

typedef struct dict {
    dictType *type;
    void *privdata;
    dictht ht[2];  //= 两个hashTable, ht[1]是rehashing时的新表
    long rehashidx; //= old_hashTable(ht[0])里将要rehash的桶的索引, 等于-1时不在rehashing[@ref dictIsRehashing()]
    unsigned long iterators; /* number of iterators currently running */
} dict;
```

# dict的rehash
- 扩展和收缩
def align(x) = pow(2, ceil(log2(x)))
扩展, ht[1].size = align(ht[0].used * 2)
收缩, ht[1].size = align(ht[0].used)
- 渐进rehash
dict同时持有ht[0]和ht[1]两张表
每次对dict的CURD都会做一次增量rehash, 把rehash分散到每个操作上
put时新kv放到ht[1], get时可能同时查两张表

# 复制
## 旧的复制
- 同步
从节点向主节点同步
流程:
  1. client向slave发slaveof命令
  2. slave向master发sync命令
  3. master收到sync命令后执行bgsave, 生成RDB文件, 同时开始记录写命令
  4. master执行完bgsave后, 把RDB文件发给slave, slave执行更新
  5. master发写命令记录给slave, slave执行更新
- 命令传播
同步操作完成后主从达到短暂的一致. 后续master向slave传播写命令来保持一致
- problems
slave初次复制没有问题, 但发生断线后重复制则效率低, 因为sync命令生成RDB文件消耗大
## 新的复制
slave用psync命令. psync包括完整复制和断线复制. 断线复制时, master只发送断线后的写命令
- 断线重复制的实现
主从各自维护`复制偏移量`. master发n个byte，以及slave收到n个字节，会使自身的offset加n
主节点有一个`复制积压缓冲区`, 是固定长度的队列, 默认1MB大小
从节点发psync命令时会传递offset, 如果offset还在复制积压缓冲区, 则执行增量复制, 否则执行全量复制
psync命令除了offset, 还会带runID(redis实例ID redis进程启动时生成的40字节uuid)
如果runID不同, 说明主节点发生过重启, 此时执行全量复制

# sentinel
sentinel系统会监视主节点和各个从节点的存活状态
当主节点下线超过用户设置的时长后, sentinel会执行故障转移, 过程如下:
  1. 把某个从节点升级成主节点
  2. 通知其他从节点向新主节点复制
  3. 若旧主节点重新上线, 则变成新主节点的从节点
- sentinel监听主节点的实现
sentinel本质是运行在特殊模式下的redis服务. 普通redis服务会载入RDB或AOF文件, sentinel由于不提供数据服务就不载入这些文件
sentinel和主节点间创建2个连接; 一个命令连接, 用于向主节点发生命令; 另一个是订阅连接, 用于订阅主节点的__sentinel__:hello信息
  在redis发布订阅中, 如果client在接收msg过程中断线, 会导致这条msg未被处理. 建立订阅连接可以保证不丢失处理
sentinel每10s向主节点发info命令来获取主服务的runID和从节点的ip集合, sentinel通过主节点来发现从节点
sentinel每10s向从节点发info命令
sentinel通过命令连接向通道__sentinel__:hello发送publish命令, msg包括自身ip_port和主节点ip_port等信息, 自身和其他监视同一主节点的sentinel都会收到msg
监视同一主节点的sentinel通过订阅主节点的通道来发现其他sentinel, 并相互之间建立命令连接
- sentinel判断主节点下线逻辑
sentinel每秒向主、从、其他sentinel发ping命令来判断在线状态
sentinel判断主节点下线后, 会向其他sentinel询问(发送命令is-master-down-by-addr,ISMD), 当有足够数量的sentinel认为主节点下线时才会执行故障转移
故障转移由局部主sentinel进行
ISMD命令的回复包括3个字段: downState(是否认同主节点下线), leaderRunID(执行故障转移的sentinel的id), leaderEpoch
超过半数投票的sentinel才会成为局部主sentinel
主sentinel选择优先级更高, 复制偏移量更大的从节点做新主节点

# 事务的实现
client发multi开始一个事务, server把后续命令放到`事务队列`里, until收到exec/watch/discard/multi
  exec, 执行事务队列里的命令
  watch, 乐观锁机制, 在multi开始事务前设置要监听的key, exec执行时如果被监听的key被设置过(包括值未改变)则不执行事务
  discard, 清空并删除当前事务队列
  multi, 可重入