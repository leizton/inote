Map.difference()
	比较两个Map对象的差异.
	3种形式:
		Maps.difference(Map left, Map right):MapDifference<K,V>
		Maps.difference(Map left, Map right, Equivalence valueEquivalence):MapDifference<K,V>
		Maps.difference(SortedMap left, SortedMap right):SortedMapDifference<K,V>


Equivalence<T>
方法
	abstract doEquivalent(T a, T b):boolean
	equivalent(T a, T b):boolean {
		if a == b  return true
		if a == null || b == null  return false
		return doEquivalent(a, b)  // 调用子类实现的方法
	}


MapDifference<K,V>
内部接口
	ValueDifference<V>
		leftValue():V   // 返回左Map的value
		rightValue():V  // 返回右Map的value
接口
	areEqual():boolean
	entriesOnlyOnLeft():Map   // left有, right没有
	entriesOnlyOnRight():Map  // right有, left没有
	entriesInCommon():Map     // left和right都有
	entriesDiffering():Map<K, ValueDifference<V>>  // key相同, value不同


Maps.doDifferenct(Map left, Map right, Equivalence valueEquivalence,
		Map onlyOnLeft, Map onlyOnRight, Map onBoth, Map<K, ValueDifference<V>> diffs) {
	// onlyOnLeft, onBoth, diffs是空的Map.
	// onlyOnRight由right复制而来.
	for Entry e : left.entrySet()
		K lk = e.getKey(), V lv = e.getValue()
		if right.contains(lk)
			V rv = onlyOnRight.remove(lk)
			if valueEquivalence.equivalent(lv, rv)
				onBoth.put(lk, lv)
			else
				diffs.put(lk, ValueDifferenceImpl.create(lv, rv))
		else
			onlyOnLeft.put(lk, lv)
}