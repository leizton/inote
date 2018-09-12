ttl     key      --剩余时间
expire  key sec
exists  key      --ret: 1,存在; 0,不存在

# 5种数据结构

# string
set     key value [ex sec] [nx|xx]  --nx,不存在时设置; xx存在时设置
setex   key ex_sec value            --带ex的set
setnx   key value                   --带nx的set
get     key
mset    k1 v1 [k2 v2]...            --原子地同时设置多个kv
msetnx  k1 v1 [k2 v2]...            --所有key不存在时变成mset

incr    key                         --原子计数器
incrby  key num
decr    key
decrby  key num

# list
lpush   key v1 v2...                --头节点插入
lpop    key                         --pop头元素
rpush   key v1 v2...                --尾节点插入
rpop    key
llen    key
lindex  key index                   --返回第index个元素,时间O(n)
lrange  key start stop              --stop可以是负数
lset    key index value             --时间O(n)

# set
sadd      key e1 e2...
srem      key e1 e2...              --移除e1 e2...
smembers  key                       --返回所有元素
sismember key e
scard     key
spop                                --随机pop一个元素
sdiff     key s1 s2                 --返回s1有s2没有的差集

# zset
--跳跃表实现
zadd      key score1 e1 [score2 e2]...
zrem      key e1 e2... [withscores]
zrank     key e                         --返回e的排名(从0开始), 按score从小到大
zrevrank  key e
zscore    key e                         --返回e的score
zcard
zcount    key score_min score_max       --score在[min,max]间的元素个数
zrange    key start stop [withscores]   --按score从小到大, stop可以是负数
zrevrange
zrangebyscore  key score_min score_max [withscores] [limit offset count]
zrevrangebyscore

# hash-table
hset      key field value
hget      key field
hmset     key f1 v1 [f2 v2]...
hmget     key f1 f2...
hexists   key field
hincrby   key field num