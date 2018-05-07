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

// Buffer
abstract
- 字段
	// mark <= position <= limit <= capacity
	// position和limit之间是未读数据, limit之后是可写区域
	int mark, position, limit, capacity;
- 实现子类
	ByteBuffer  CharBuffer   ShortBuffer  IntBuffer
	LongBuffer  FloatBuffer  DoubleBuffer