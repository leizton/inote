包 com.google.common.base


Strings, 字符串工具类
方法
	isNullOrEmpty(String):boolean
	nullToEmpty(String s):String  原对象s仍然是null, 返回值是""
	emptyToNull(String s):null  原对象仍然是""
	commonPrefix(CharSequence s1, CharSequence s2)  最长公共前缀
		("123", "1234") -> "123"
	commonSuffix(CharSequence s1, CharSequence s2)  最长公共后缀
		("123", "4123") -> "123"


Ints
方法
	contains(int[], int target):boolean
	max(int...):int  如max(1,2,3):3
	min(int...):int  如min(new int[] {1,2,3}):1
	concat(int...):int[]
	asList(int...):List<Integer>
		Arrays.asList(new int[] {1,2,3}).size() == 1
		Arrays.asList(T...)会把数组对象当成一个对象放入新的List中
		Ints.asList(new int[] {1,2,3}).size() == 3
		Ints.asList()返回IntArrayAsList对象, 不支持List.add(e)方法, 抛异常UnsupportedOperationException


Preconditions
方法
	static <T> checkNotNull(T ref):T {
		if ref == null
			throw new NullPointerException();
		return ref
	}
	static checkArgument(boolean expression,
		@Nullable String errorMessageTemplate, @Nullable Object... errorMessageArgs) {
		if (!expression)
			throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs))
	}


Objects
	// 可以用于判断两个null, 使用a.equals(b)时若a是null会抛空指针异常
	equal(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}


CharMatcher
例子
	CharMatcher.anyOf("abc").removeFrom("a1b2c3")  // "123"
	CharMatcher.anyOf("abc").retainFrom("a1b2c3")  // "abc"
	CharMatcher.anyOf("abc").or(CharMatcher.is('1')).removeFrom("a1b2c3")  // "23"
	Splitter.on(",").trimResults(CharMatcher.anyOf("a ")).omitEmptyStrings().splitToList(",a ,b,c ,")  // [b, c]  有2个元素


Ordering
	Ordering<T> order = new Ordering<T>() {
		@Override
		public int compare(T left, T right) {
			return -1;
		}
	};
	// 计算topK
	order.greatestOf(iterable/iterator, topK);


Optional
静态方法
	// 返回不可用对象
	<T> Optional<T> absent() {
		return Absent.withType()
	}
	// 返回可用对象
	<T> Optional<T> of(T reference) {
		return new Present<T>(checkNotNull(reference))
	}
方法
	isPresent():boolean  // 是否可用
	get():T              // 获取可用对象


Function<F,T>
接口
	apply(F input):T
	equals(Object object):boolean