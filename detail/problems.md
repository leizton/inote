#
1. 由于是构造一个简单的pojo对象, 所以没有try-catch这个对象的构造的异常, 最终抛异常后导致Schedule线程池的任务不再定时执行了. 这个线程池会吞掉异常且没有日志等输出, 并且停止调度. 
   经验: 尽量多try-catch.
2. DefaultPullConsumer里由于PlainPullEntry的pull方法的参数size定义不明确(变量名取得不合适), 使得写调用该方法的代码时传入的参数多减了output.size.
   经验: 方法名和参数名应该做到足够表达含义, 否则必须加足够的注释说明. 调用一个方法时, 应该明确参数的含义, 如果不能通过参数名获知, 应该看代码实现来了解.
3. store会定时删除messageLog而不清理consumeLog, 当pull message时, server端拿到consumeLogOffset和messageLogOffset后不能find到message, 并且store返回的查找结果是success, 导致server把空的SelectSegmentBufferResult写到channel.
   经验: 修改操作应该保证让使用者看到的数据是一致的, 除非不一致没有影响. 返回结果不能随意是success, 应该准确.
4. server出现consumer传来的last-pullLogOffset大于server最后一次传给consumer的pullLogOffset
   原因: server已经发送给了client, 当client在接收过程中出现网络断开或超时(断开后会重连), 导致client没有收到, 所以client重新发送上一次请求参数
   解决: 重试
5. 做.net-client时发现msg里设置浮点数(float double)时, 由于使用toString序列化, 导致接收时==比较结果是false(如传递double.MAX_VALUE), 把double值转成long表示才能使==比较结果是true
   经验: float和double应设置精度, 相等比较通过绝对差值是否小于精度实现.