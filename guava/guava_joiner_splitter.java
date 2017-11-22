Joiner
例子
	Joiner.on("+").join(Lists.newArrayList(1,2,3))  // "1+2+3", 不能有null元素
	Joiner.on("+").skipNulls().join(Lists.newArrayList(1,2,null,3))  // "1+2+3"
	Joiner.on("+").useForNull("0").join(Lists.newArrayList(1,2,null,3))  // "1+2+0+3"
	Joiner.on("&").withKeyValueSeparator("=").join(params)  // "a=1&b=2", params是Map对象{a=1, b=2}
说明
	skipNulls()和useForNull()返回的Joiner分别重写了useForNull和skipNulls, 在里面抛出异常UnsupportedOperationException, 从而使两者不能同时使用.
	join使用StringBuilder存放结果, skipNulls和useForNull返回的Joiner重写了appendTo()方法, 在产生结果时增加了新的规则.
内部类
	MapJoiner(Joiner joiner, String keyValueSeparator) {
		this.joiner = joiner
		// import static com.google.common.base.Preconditions.checkNotNull;
		this.keyValueSeparator = checkNotNull(keyValueSeparator)
	}
方法
	on(String separator):Joiner {
		return new Joiner(separator)
	}
	join(Iterable<?> parts):String {
		return appendTo(new StringBuilder(), parts.iterator()).toString()
	}
	appendTo(StringBuilder builder, Iterator<?> parts):final StringBuilder {
		try
			appendTo((Appendable) builder, parts)
		catch IOException impossible
			throw new AssertionError(impossible)
		return builder
	}
	<A extends Appendable> A appendTo(A appendable, Iterator<?> parts) throws IOException {
		checkNotNull(appendable)
		if parts.hasNext()
			appendable.append(toString(parts.next()))
			while (parts.hasNext())
				appendable.append(separator)
				appendable.append(toString(parts.next()))
		return appendable
	}
	skipNulls():Joiner {  // 跳过null元素
		return new Joiner(this) {
			@Override
			<A extends Appendable> A appendTo(A appendable, Iterator<?> parts) throws IOException {
				checkNotNull(appendable, "appendable")
				checkNotNull(parts, "parts")
				while parts.hasNext()  // 找到第一个非null元素, 并输出
					Object part = parts.next()
					if part != null
						appendable.append(toString(part))
						break
				while parts.hasNext()
					Object part = parts.next()
					if part != null
						appendable.append(separator)
						appendable.append(toString(part))
				reutrn appendable
			}
			@Override
			Joiner useForNull(String nullText) {  // 排斥调用useForNull()
				checkNotNull(nullText)
				throw new UnsupportedOperationException("already specified skipNulls")
			}
			...
		}
	}
	useForNull(String s):Joiner  用s替换null元素, 不能和skipNulls()同时使用
	withKeyValueSeparator(String s):MapJoiner {
		return new MapJoiner(this, keyValueSeparator)
	}


Splitter
例子, 用法类似Joiner
	Splitter.on(",").splitToList(", a ,b,c ,")  // [,  a , b, c ,]  有5个元素
	Splitter.on(",").trimResults().splitToList(", a ,b,c ,")  // [, a, b, c, ]  有5个元素
	// trimResults和omitEmptyStrings的顺序可交换
	Splitter.on(",").trimResults().omitEmptyStrings().splitToList(", a ,b,c ,")  // [a, b, c]  有3个元素
	Splitter.on("&").withKeyValueSeparator("=").split("a=1&b=2")  // {a=1, b=2} 分割成Map对象
内部类
	MapSplitter
	方法
		MapSplitter(Splitter outerSplitter, Splitter entrySplitter) {
			this.outerSplitter = outerSplitter
			this.entrySplitter = entrySplitter
		}
		split(CharSequence seq):Map<String, String> {
			Map<String, String> map = new LinkedHashMap<~>()
			for (String entry : outerSplitter.split(seq)
				Iterator<String> entryFields = entrySplitter.spliterator(entry)
				// key
				checkArgument(entryFields.hasNext(), INVALID_ENTRY_MESSAGE, entry)
				String key = entryFields.next()
				checkArgument(!map.containsKey(key), "Duplicate key [%s] found.", key)
				// value
				checkArgument(entryFields.hasNext(), INVALID_ENTRY_MESSAGE, entry)
				String value = entryFields.next()
				map.put(key, value)
				checkArgument(!entryFields.hasNext(), INVALID_ENTRY_MESSAGE, entry)
			return Collections.unmodifiableMap(map)
		}
	SplittingIterator
	抽象方法
		separatorStart(int start):int  // 返回从start开始的第一个分割符位置
		separatorEnd(int separatorPosition):int
	方法
		# computeNext():String  // 每次返回一个分割出的元素
内部接口
	Strategy {
		iterator(Splitter splitter, CharSequence toSplit):Iterator<String>
	}
字段
	- final Strategy strategy
静态方法
	on(String separator):Splitter {
		checkArgument(separator.length() != 0, "The separator can't be the empty string.")
		if separator.length() == 1
			return on(separator.charAt(0))
		
		return new Splitter(
			new Strategy() {
			iterator(Splitter splitter, CharSequence toSplit):SplittingIterator @Override {
				return new SplittingIterator(splitter, toSplit) {
					separatorStart(int start):int @Override {
						int separatorLength = separator.length()
						positions:
						for p = start, last = toSplit.length()-separatorLength; p <= last; p++
							for i = 0; i < separatorLength; i++  // 比较出完整的separator
								if toSplit.charAt(p+i) != separator.charAt(i)
									continue positions  // 因为后面有return, 所以不用break
							return p
						return -1;
					}
					separatorEnd(int separatorPosition) {
						return separatorPosition + separator.length()
					}
				}
			}
		})
	}
方法
	split(final CharSequence seq):Iterable<String> {
		checkNotNull(seq)
		return new Iterable<String> {
			iterator():Iterable<String> @Override {
				return spliterator(seq)
			}
			toString():String @Override {
				return Joiner.on(", ").
					appendTo(new StringBuilder().append('['), this).
					append(']').toString()
			}
		}
	}
	- spliterator(CharSequence seq) {
		return strategy.iterator(this, seq)
	}