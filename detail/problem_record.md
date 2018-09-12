#
1. 由于是构造一个简单的pojo对象, 所以没有try-catch这个对象的构造的异常, 最终抛异常后导致Schedule线程池的任务不再定时执行了. 这个线程池会吞掉异常且没有日志等输出, 并且停止调度. 
  经验: 尽量多try-catch. 交给线程池执行的run()里必须try-catch
2. DefaultPullConsumer里由于PlainPullEntry的pull方法的参数size定义不明确(变量名取得不合适), 使得写调用该方法的代码时传入的参数多减了output.size.
  经验: 方法名和参数名应该做到足够表达含义, 否则必须加足够的注释说明. 调用一个方法时, 应该明确参数的含义, 如果不能通过参数名获知, 应该看代码实现来了解
3. store会定时删除messageLog而不清理consumeLog, 当pull message时, server端拿到consumeLogOffset和messageLogOffset后不能find到message, 并且store返回的查找结果是success, 导致server把空的SelectSegmentBufferResult写到channel.
  经验: 修改操作应该保证让使用者看到的数据是一致的, 除非不一致没有影响. 返回结果不能随意是success, 应该准确
4. server出现consumer传来的last-pullLogOffset大于server最后一次传给consumer的pullLogOffset
  原因: server已经发送给了client, 当client在接收过程中出现网络断开或超时(断开后会重连), 导致client没有收到, 所以client重新发送上一次请求参数
  解决: 重试
5. 做.net-client时发现msg里设置浮点数(float double)时, 由于使用toString序列化, 导致接收时==比较结果是false(如传递double.MAX_VALUE), 把double值转成long表示才能使==比较结果是true
  经验: float和double应设置精度, 相等比较通过绝对差值是否小于精度实现
6. [12:46:20] find lost ack messages consumer:XXX001, pullLogOffsetInConsumer:93570, pullLogOffsetInServer:93590, requestNum:20, result:PullMessageResult{, pullLogOffset=93571, bufferTotalSize=0, messageNum=0}
  [12:46:21] lost ack count, consumer:XXX001, confirmedOffset:93570, firstPullOffset:93591
  由于broker返回给consumer的[93571,93590]这段msg实际是空list(PullMessageResult.msgNum=0), consumer在ack上也就丢失了这段, 所以出现后面的'lost ack count'
  broker取出空的msg时, 机器监控的磁盘出现iowait, 具体原因还需调查
7. 运行数月才发现server上丢失msg未传给client
  原因:
    在DefaultMessageStore#getMessage(String,long,int)里"getMessageResult.setNextBeginOffset(consumerLog.getMaxOffset());"
    这里设置nextBeginOffset时不能再调用getMaxOffset(), 因为下一次消费点(offset)比前一步获取到的offset更大的原因除了其他consumer抢用, 还可能是新msg导致consumerLogSeq涨了.
    如果是新msg, 则下一次消费点应该就是前一步获取到的offset. 所以本质原因是并行运行的生产线程(append-msg)和消费线程(get-msg)依赖(共享)了同一个变量(maxOffset)
  经验:
    必要的检查不能省, 如果检查了nextBeginOffset是否连续, 则不会运行数月才发现.
    编程时, 应思考清楚每个变量表达的含义, 一个变量不能代表多个含义应该符合单一职责原则(maxOffset只给生产线程使用).
    确保每个变量修改是正确的(上面的问题是对nextBeginOffset的修改不正确导致).
    锁能保证原子性和强一致性, 有时只需保证最终一致性则可用volatile. 多数情况下无需考虑变量的持久性.
8. 使用Hikari连接池抛RuntimeException
  异常在stderr或localhost里
  没有在jdbcUrl设置autoReconnect, 导致抛: No operations allowed after connection closed
  这个RuntimeException是main线程抛的, 由于存在线程池非daemon, 使得进程没有彻底退出
  more autoReconnect follow:
    https://github.com/brettwooldridge/HikariCP/issues/289