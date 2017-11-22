创建List
	Lists.newArrayList()
	Lists.newArrayList("a", "b", "c")
	Lists.newArrayListWithCapacity(100)

创建Map
	Maps.newHashMap()
	Maps.newHashMapWithExpectedSize(100)
	HashMultimap.create():SetMultimap<K,V>
	ArrayListMultimap.create():ListMultimap<K,V>

HashBiMap<K,V> 需要每个V也是唯一的, 实现K到V和V到K的查询.

AtomicLongMap<K> 对value(AtomicLong型)的操作是线程安全的
	addAndGet(K, long l)  返回value增加l后的值
	getAndAdd(K, long l)  返回value增加前的值
	incrementAndGet(K)  前缀++
	decrementAndGet(K)  前缀--
	put(K, long)
	putIfAbsent(K, long)  不存在K时放入
	remove(K):long
	sum():long