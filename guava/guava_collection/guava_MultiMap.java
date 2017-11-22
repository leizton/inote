/**
抽象类AbstractMapMultimap和AbstractMapBasedMultimap实现MultiMap的大部分接口
抽象类AbstractListMultimap继承AbstractMapBasedMultimap和ListMultiMap,
	把Collection<V>返回类型替换成List<V>, 相当于一个适配器.
AbstractMapBasedMultimap的createCollection()抽象方法留给子类实现
ArrayListMultiMap继承AbstractListMultimap实现createCollection(), 使得Collection<V>是ArrayList<V>
*/


--------------------------------------------------------------------------------
MultiMap<K,V>
子接口
	ListMultiMap, SetMultiMap, SortedSetMultiMap
实现类
	ArrayListMultiMap
接口
	put(K, V)  putAll(K, Iterable<? extends V>)
	size()返回entry<K,V>数
	keySet():Set<K>
	get(K):Collection<V>
	containsKey(K):boolean  containsEntry(K, V):boolean
	remove(K,V):boolean  removeAll(K):Collection<V>
	replaceValues(K, Iterable<? extends V>):Collection<V>

--------------------------------------------------------------------------------
AbstractMultimap
	implements MultiMap
抽象方法
	entryIterator():Iterator<Entry<K,V>>
方法
	isEmpty():boolean
	containsEntry(K, V):boolean

--------------------------------------------------------------------------------
AbstractMapBasedMultimap
	extends AbstractMultimap
	implements Serializable
字段
	- transient Map<K, Collection<V>> map
	- transient int totalSize
抽象方法
	createCollection():Collection<V>
方法
	put(K key, V value):boolean {
		Collection<V> c = map.get(key)
		if c == null
			c = createCollection()
			if c.add(value)
				totalSize++
				map.put(key, c)
				return true
			else
				throw new AssertionError("...")
		else if c.add(value)
			totalSize++
			return true
		else
			return false
	get(K key):Collection<V> {
		Collection<V> c = map.get(key)
		if c == null
			c = createCollection(key)
		return wrapCollection(key, c)
	}
	wrapCollection(K key, Collection<V> c):Collection<V> {
		if (c instanceof SortedSet)  // SortedSet必须在Set前面
			return new WrapperSortedSet(key, (SortedSet<V>) c, null)
		else if (c instanceof Set)
			return new WrapperSet(key, (Set<V>) c)
		else if (c instanceof List)
			return wrapperList(key, (List<V>) c, null)
		else
			return new WrapperCollection(key, c, null)
		/**
		wrapper即包装器/装饰器, 增强功能, 如Integer包装int
			WrapperCollection继承AbstractCollection, 所以可当成Collection用
		代理模式, 提供对被代理对象的访问控制
		适配器模式, 改变接口形式
		外观模式, 用一个接口封装多个子接口, 使得高层使用更方便
		*/
	}
	clear() {
		for Collection<V> c : map.values()
			c.clear()  // 防止内存泄漏
		map.clear()
		totalSize = 0
	}
	containsKey(K):boolean
	replaceValues(K, Iterable<? extends V>):Collection<V>
	removeAll(Object):Collection<V>

--------------------------------------------------------------------------------
ListMultiMap
接口
	get(K):List<V>
	removeAll(K):List<V>
	replaceValues(K, Iterable<? extends V>):List<V>

--------------------------------------------------------------------------------
AbstractListMultimap
	extends AbstractMapBasedMultimap
	implements ListMultimap
	相当于适配器, 把AbstractMapBasedMultimap的方法返回类型从Collection<V>适配成List<V>

--------------------------------------------------------------------------------
ArrayListMultiMap
	extends AbstractListMultimap
抽象方法实现
	createCollection():List<V> {
		return new ArrayList<V>(expectedValuesPerKey);
	}