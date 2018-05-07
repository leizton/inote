// equals()
equals(Object o):boolean
	if this == o, => true
	if o == null || getClass() != o.getClass(), => false
	Foo that = (Foo) o
	...

// Class.getName()/getCanonicalName()
getName() => package.Foo$Inner
getCanonicalName() => package.Foo.Inner

// Class.cast()
.cast(Object obj):T
	if obj != null && !isInstance(obj)
		throw ClassCastException
	=> (T) obj

// 行分割符
System.getProperty("line.separator")

// BigDecimal Impl
intCompact:long, intVal:BigInteger
precision:int, scale:int