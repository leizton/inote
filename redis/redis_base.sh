#################################################
# 有序集合
$ zadd key score member [score member ...]
  # 时间复杂度 O(M * logN), M插入个数, N有序集的基数

$ zcard key
  # 返回有序集的基数

$ zrange key score_start score_stop [WITHSCORES]
  # WITHSCORES返回value的score值, 列表按score从小到大排序

$ zrevrange 同上
  # 列表按score从大到小排序


#################################################
# 列表
$ lpush key value [value ...]
  # 插入到列表左边(头部, l指left), 时间O(1)
  # lpush languages python python c++  可放入重复元素
  # lrange languages 0 -1  上面push结果: c++ python python

$ rpush key value [value ...]
  # 插入到列表右边(尾部)

$ lrange key start stop
  # stop = -1, 最后一个元素; stop = -2, 倒数第2个元素

$ llen key
  # 列表长度, 时间O(1)

$ ltrim key start stop
  # 删除不在区间[start, stop]上的元素, 即只保留区间[start, stop]上的元素
  # 若start > stop, 则清空列表, 如ltrim foolist 1 0
  # ltrim foolist -1 0  也是清空列表, 因为-1其实是(llen foolist) - 1

$ blpop key [key ...] timeout
  # 阻塞式弹出一个头部元素
  # exists foolist     确保foolist不存在
  # blpop foolist 300  由于foolist不存在, 会阻塞等待另一个client对foolist作lpush, 超时时间300秒

$ brpop key [key ...] timeout