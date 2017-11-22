ConcurrentMap<K,V>
	extends Map<K,V>
接口
	getOrDefault(Object key, V):V
	putIfAbsent(K,V):V
		等价于 return map.containsKey(key) ? map.get(key) : map.put(key,value)
		当key absent(不在)时, 进行put(key,value), 返回null
	remove(Object key, Object value):boolean
	/**
	 * BiFunction<T,U,R> 表示接收2个参数(T,U)返回1个结果(R)的一类函数
	 * remap 是re-mapping, 重新生成映射对
	 */
	compute(K key, BiFunction<? super K, ? super V, ? extends V> remap) {
		Objects.requireNonNull(remap)
		oldV = get(key)
		while 1
			newV = remap.apply(key, oldV)
			if newV == null  // newV是null, 则需删除key的映射对
				if oldV != null || containsKey(key)
					if remove(key, oldV)  // true, 则apply()过程中oldV没有发生改变, 这是多线程环境
						return null
					oldV = get(key)  // oldV被其他线程更新了
				else  // key的映射对本身就不在Map中, 或其value是null, 则无需操作
					return null
			else
				if oldV != null
					if replace(key, oldV, newV)  // true, 则apply()过程中oldV没有发生改变
						return newV
					oldV = get(key)
				else
					oldV = putIfAbsent(key, newV)
					if oldV == null  // 上一步put成功
						return newV
	}


ConcurrentHashMap
	extends AbstractMap
	implements ConcurrentMap
方法
	spread(int hashCode):int static final {
		// HASH_BITS = 0x7fffffff, 和HASH_BITS与可限制结果是非负数
		return (hashCode ^ (hashCode >>> 16)) & HASH_BITS
	}
	/* get()不加锁, 实现和HashMap类似 */
	get(Object key):V {
		Node<K,V> e, p
		h = spread(key.hashCode())  // 没有检查key非null, 所以不能调用concMap.get(null)
		if table != null && table.length > 0 &&
			( e = tabAt(table, h & (table.length-1)), e != null )
			if e.hash == h
				if e.key == key || key.equals(e.key)
					return e.val
			elif e.hash < 0
				p = e.find(h, key)
				return p != null ? p.val : null
			while (e = e.next, e != null)
				if e.hash == h && (e.key == key || key.equals(e.key))
					return e.val
		return null
	}
	put(K key, V value):V {
		return putVal(key, value, false)
	}
	/* putVal()在桶上加锁 */
	putVal(K key, V value, boolean onlyIfAbsent):V final {  // put(K,V) 和 putIfAbsent() 的具体实现
		if key == null || value == null
			throw new NullPointerException()
		hash = spread(key.hashCode())
		binCnt = 0
		for Node<K,V>[] tab = table; 1;
			if tab == null || tab.length == 0
				tab = initTable()
			elif f = tabAt(tab, i = hash & tab.length), f == null
				// casTabAt()是compare and swap, 值仍等于形参时, 交换引用
				if casTabAt(tab, i, null, new Node<K,V>(hash, key, value, null))
					break;  // 放入到空桶时不加锁
			elif fh = f.hash, fh == MOVED  // MOVED == -1
				tab = helpTransfer(tab, f)
			else
				synchronized (f)  // 仅是在f上加锁
				if tabAt(tab, i) == f
					if fh >= 0
						// 向该桶中放入<key,value>
						for Node<K,V> e = f, binCnt = 1; ; binCnt++
							if e.hash == hash && key.equals(e.key)
								oldV = e.val
								if !onlyIfAbsent
									e.val = value
								break
							pred = e
							if e = e.next, e == null
								pred.next = new Node<K,V>(hash, key, value, null)
								break
					elif f instanceOf TreeBin
						// 树型桶
						binCnt = 2
						if p = (TreeBin<K,V>) f, p.putTreeVal(hash, key, value) != null
							oldV = p.val
							if !onlyIfAbsent
								p.val = value
					// fh < 0, 则 binCnt == 0
				-- synchronized (f)
				if binCnt != 0
					if binCnt > TREEIFY_THRESHOLD  // 转成红黑树结构
						treeifyBin(tab, i)
					if oldV != null
						return oldV
					break
		-- for binCnt==0时, 会继续for循环
		addCount(1, binCnt)
		return null
	}
	addCount(long x, int check) {
	}