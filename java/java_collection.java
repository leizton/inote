List: ArrayList LinkedList
Set:  HashSet LinkedHashSet TreeSet
Map:  HashMap LinkedHashMap TreeMap

jdk7前	List<E> list = new ArrayList<E>();
jdk7	 List<E> list = new ArrayList<>();
Guava	 List<E> list = Lists.newArrayList();

map.put(key, map.containsKey(key) ? map.get(key) + 1 : 1);
containsKey(key) 检查key是否存在
map.get(key)==null 可能是containsKey(key)==false, 或者当key存在, value是null
慎用下面代码
	V value = map.get(key);
	if (value != null) value = value + 1;
	else map.put(1);
只有当每个key对应的value不会是null时才可.

HashMap的桶个数设成2的幂次原因是(2^n-1)是梅森素数.

HashMap的遍历顺序和key的Hash值有关
LinkedHashMap, 当accessOrder==false时, 遍历顺序和put时的顺序有关,
	当accessOrder==true时, get()方法会调用afterNodeAccess()改变遍历顺序, 可用于实现LRU.

ArrayList
字段
	transient Object[] elementData  // 数组对象
方法
	trimToSize() {  // 把capacity调到size
		modCount++  // 父类AbstractList的统计修改次数
		if size < elementData.length
			elementData = (size == 0)
				? EMPTY_ELEMENTDATA
				: Arrays.copyOf(elementData, size)
	}
	clear() {
		modCount++
		for i = [0, size)
			elementData[i] = null  // 加快内存回收
		size = 0
	}

LinkedHashMap
	extends HashMap
内部类
	Entry<K,V>
		extends HashMap.Node<K,V>
	字段: Entry<K,V> before, after
字段
	LinkedHashMap.Entry<K,V> head;  // 最老的元素
	LinkedHashMap.Entry<K,V> tail;  // 最新(最近访问)的元素
	final boolean accessOrder;
方法
	// 没有实现put()方法, 直接是HashMap的put().
	get(Object key):V {
		Node<K,V> e = getNode(hash(key), key)  // getNode()和hash()是HashMap的方法
		if e == null
			return null
		if accessOrder
			afterNodeAccess(e)
		return e.value
	}
	/* HashMap的putVal(int hash, K, V, boolean onlyIfAbsent, boolean evict)
	 * 在找到key时, 调用afterNodeAccess(e)
	 * 在找不到key需要插入新<K,V>对时, 调用afterNodeInsertion(evict)
	 * onlyIfAbsent是true时, 找到key后不会替换原Value
	 * evict(驱逐)是true时, 对于LinkedHashMap可能删除最老元素
	*/
	afterNodeInsertion(boolean evict) {  // 当元素被插入后. 该方法是重写HashMap的afterNodeInsertion().
		LinkedHashMap.Entry<K,V> first
		if evict && (first = head) != null && removeEldestEntry(first)
			K key = first.key
			removeNode(hash(key), key, null, false, true)  // 删除最老元素
	}
	afterNodeAccess(Node<K,V> e) {  // 当元素被访问后
		LinkedHashMap.Entry<K,V> last
		if accessOrder && (last = tail) != e  // e不是最新元素, 因为有元素e所以tail != null
			p = (LinkedHashMap.Entry<K,V>) e
			b = p.before, a = p.after
			p.after = null
			// 移除元素p
			b==null ? head=a : b.after=a
			a==null ? last=b : a.before=b
			// 下面是linkNodeLast()里的代码
			if last != null
				p.before = last
				last.after = p
			else
				head = p
	}
	# removeEldestEntry(Map.Entry<K,V> eldest) {  // 可被子类重写(Override)
		return false
	}
	/* HashMap的putVal()会调用newNode()
	 * newNode(int hash, K, V, Node<K,V> e)
	 * newTreeNode(int hash, K, V, Node<K,V> next)
	 * 会调用linkNodeLast()
	*/
	linkNodeLast(LinkedHashMap.Entry<K,V> p) {  // 把元素p链到链表最后一个元素
		last = tail; tail = p
		if last != null
			p.before = last
			last.after = p
		else
			head = p
	}