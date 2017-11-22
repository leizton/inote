包 com.google.common.cache


使用
LoadingCache<Integer,String> cache = CacheBuilder.newBuilder().build(
	new CacheLoader<>() {
		load(Integer key):String @Override {
			return String.format("num %d", key)
		}
	});


CacheBuilder<K,V>
静态方法
	newBuilder():CacheBuilder<Object,Object> {
		return new CacheBuilder()
	}
	build():Cache<K1 extends K, V1 extends V> {
		return new LocalCache.LocalManualCache<K1,V1>(this)
	}
	build(CacheLoader<? super K1, V1> loader) {
		return new LocalCache.LocalLoadingCache<K1,V1>(this, loader)
	}
	// Key和Value的引用类型, 默认是强引用
	setKeyStrength(Strength strength):CacheBuilder
	getKeyStrength():Strength {
		return MoreObjects.firstNonNull(KeyStrength, Strength.STRONG)
		/*
			firstNonNull(T first, T second):T static<T> {
				return first != null ? first : checkNotNull(second);
			}
		*/
	}
	setValueStrength(Strength strength):CacheBuilder
	getValueStrength():Strength {
		return MoreObjects.firstNonNull(valueStrength, Strength.STRONG)
	}


Cache<K,V>
接口
	getIfPresent(Object key):V
	get(K key, Callable<? extends V> valueLoader):V  // if cached, return; else create, cache and return.
	put(K, V)
	invalidate(Object key)  // 删除key的缓存
	size():long  // 缓存实体的个数(近似值)
	asMap():ConcurrentMap<K,V>
	stats():CacheStats  // 当前状态的快照


CacheStats
	缓存的状态信息
字段
	// 命中次数, 未命中次数, 调用CacheLoader的load成功和失败次数
	- long histCount, missCount, loadSuccessCount, loadExceptionCount, totalLoadTime, evictionCount(移除);


/**

LocalCache存放CacheEntry(缓存key-value对)的结构类似ConcurrentHashMap.
LocalCache有一个Segment数组(final Segment<K, V>[] segments), 存放多个segment.
Segment有一个原子引用数组: volatile AtomicReferenceArray<ReferenceEntry<K, V>> table,
	数组table的元素类型ReferenceEntry, 是单链表的节点类型.
CacheEntry的hash过程分两级:
	第一级, 用哈希值的高位索引Segment数组的某个segment
		LocalCache的segmentFor()方法代码如下:
		segments[(hash >>> segmentShift) & segmentMask]
		segmentMask是Segment数组的长度, segmentMask = pow(2, 32 - segmentShift)
	第二级, 类同于HashMap的hash过程
		segment.getFirst(int hash)由hash值找到链表头节点, 如下:
			table.get(hash & (table.length() - 1))
		segment.getEntry(Object key, int hash)
			先调用getFirst(hash)找到头节点, 再遍历链表找到CacheEntry的引用

Segment的get(K key, int hash, CacheLoader loader)主要代码如下
V get(K key, int hash, CacheLoader<? super K, V> loader) throws ExecutionException {
    if (count != 0) { // segment里没有cacheEntry
        ReferenceEntry<K, V> e = getEntry(key, hash);  // 尝试从table中找到cacheEntry
        if (e != null) {
            long now = map.ticker.read();
            V value = getLiveValue(e, now);  // if e无效(key是null)/被收集(value是null)/过期, 清理, return null;
                                             // else return e.
            if (value != null) {
                recordRead(e, now);  // 更新e的访问时间
                statsCounter.recordHits(1);
                // scheduleRefresh()计划性地更新e.value,
                // 即再次执行 e.value = loader.load(e.key).
                // 可能立即返回新的value, 也可能是旧的value.
                // LocalCache的refreshNanos大于0且超时, 才更新.
                return scheduleRefresh(e, key, hash, value, now, loader);
            }
            ValueReference<K, V> valueReference = e.getValueReference();
            if (valueReference.isLoading()) {
                // waitForLoadingValue()调用ValueReference接口的waitForValue():V
                return waitForLoadingValue(e, key, valueReference);
            }
        }  // e != null
    }  // count != 0
    // 加锁后先判断table是否已经有了key(排除其他线程已经创建该key),
	// 若没有则调用loader创建新cacheEntry, 若有则返回
    return lockedGetOrLoad(key, hash, loader);
}

*/


LocalCache<K,V>
内部类
	LocalManualCache<K,V>
		implements Cache<K,V>
	字段
		final LocalCache<K,V> localCache
	构造器
		LocalManualCache(CacheBuilder b) {
			this( new LocalCache(b, null) )
		}
		LocalManualCache(LocalCache lc) {
			this.localCache = lc
		}
	LocalLoadingCache<K,V>
		extends LocalManualCache implements LoadingCache
	构造器
		LocalLoadingCache(CacheBuilder b, CacheLoader l) {
			super( new LocalCache(b, checkNotNull(l)) )
		}
	方法
		get(K key):V throws ExecutionException {
			return localCache.getOrLoad(key)  // localCache是父类LocalManualCache的字段
		}
		getUnchecked(K key):V {
			try
				return get(key)
			catch ExecutionException e
				throw new UncheckedExecutionException(e.getCause())
		}
		// 实现接口com.google.common.base.Function<F,T>
		// 接口LoadingCache扩展了Function和Cache
		apply(K key):V {
			return getUnchecked(key)
		}
字段
	final StatsCounter globalStatsCounter
	final Segment<K, V>[] segments
	long expireAfterAccessNanos  // 定义缓存对最后一次访问后, 经过多久被清除
	long expireAfterWriteNanos  // 定义缓存对最后一次写后, 经过多久被清除
	long refreshNanos  // 定义缓存对最后一次写后, 经过多久进入refresh队列, <=0时永不更新缓存对的value
方法
	segmentFor(int hash):Segment<K,V> {  // 获取某段, 类似jdk7的ConcurrentHashMap
		// 用hash值的高bit位索引segment
		return segments[(hash >>> segmentShift) & segmentMask]
	}
	// get方法
	get(Object key):V {
		if key == null -> return null
		int hash = hash(key)
		return segmentFor(hash).get(key, hash)
	}
	getIfPresent(Object key):V {
		hash = hash(checkNotNull(key))
		V value = segmentFor(hash).get(key, hash)
		if value == null
			globalStatsCounter.recordMisses(1)
		else
			globalStatsCounter.recordHits(1)
		return value
	}
	// 带cacheLoader的get
	get(Object key, CacheLoader loader) throws ExecutionException {
		hash = hash(checkNotNull(key))
		return segmentFor(hash).get(key, hash, loader)
	}
	getOrLoad(K key):V throws ExecutionException {
		return get(key, defaultLoader)  // 调用上面get(Object, CacheLoader)方法
	}
	// Key和Value的引用类型
	usesKeyReferences():boolean -> return keyStrength != Strength.STRONG
	usesValueReferences():boolean -> return valueStrength != Strength.STRONG


LocalCache.Segment
	extends ReentrantLock
	是LocalCache的内部类
构造器
	Segment(LocalCache map, int initialCapacity, long maxSegmentWeight, StatsCounter statsCounter) {
		initTable( newEntryArray(initialCapacity) )
		keyReferenceQueue = map.usesKeyReferences() ? new ReferenceQueue<K>() : null
		valueReferenceQueue = map.usesValueReferences() ? new ReferenceQueue<K>() : null
	}
	// 创建原子引用数组, 对数组元素的set操作是原子的, get操作具有volatile特性
	newEntryArray(int size):AtomicReferenceArray<ReferenceEntry<K,V>> {
		return new AtomicReferenceArray<ReferenceEntry<K,V>>(size)
	}
	initTable(AtomicReferenceArray<ReferenceEntry<K,V>> newTable) {
		this.threshold = newTable.length() * 3 / 4  // loadFactor是0.75
		if !map.customWeigher() && this.threshold == maxSegmentWeight
			this.threshold++
		this.table = newTable
	}
方法
	get(K key, int hash, CacheLoader loader):V {
		try
			ReferenceEntry<K,V> e
			if count != 0 && (e = getEntry(key, hash)) != null
				// getEntry()是从table(原子引用数组)中获取key相等的元素(缓存对Entry的Reference), 返回值可能是null
				long now = map.ticker.read()  // 从LocalCache的Ticker对象创建起到现在的纳秒数
				V value = getLiveValue(e, now)  // if e无效(key是null)/被收集(value是null)/过期, 清理, return null;
												// else return e.
				if value != null
					recordRead(e, now)  // 更新e的访问时间
					statsCounter.recordHits(1)
					/* scheduleRefresh()计划性地更新e.value,
					 * 即再次执行 e.value = loader.load(e.key).
					 * 可能立即返回新的value, 也可能是旧的value.
					 * LocalCache的refreshNanos大于0且超时, 才更新.
					*/
					value = scheduleRefresh(e, key, hash, value, now, loader)
					return value
				else // value == null
					ValueReference<K,V> vr = e.getValueReference()
					if vr.isLoading()
						// waitForLoadingValue()调用ValueReference接口的waitForValue():V
						value = waitForLoadingValue(e, key, vr)
						return value
			else  // count == 0 或 e == null, 即没有key的缓存对
				// lockedGetOrLoad这个方法名取得很好.
				// 当前线程加锁时可能被其他线程抢掉创建key的新缓存对的机会.
				// 这样需要在加锁后先判断table是否已经有了key排除其他线程创建了key的缓存对,
				// if no key, 调用loader创建新cacheEntry; else return.
				return lockedGetOrLoad(key, hash, loader)
		catch ExecutionException e
			// 抛出异常
		finally
			postReadCleanup()
	}
	recordRead(ReferenceEntry e, long now) {
		if map.recordsAccess()
			e.setAccessTime(now)  // 更新e的访问时间
		recencyQueue.add(e)
	}
	get(Object Key, int hash):V {
		
	}