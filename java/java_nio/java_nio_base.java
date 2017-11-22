/* Buffer */
缓冲类提供了对缓冲数据的结构化访问, 如维护读写位置
java.nio.Buffer abstract
	// mark <= position <= limit <= capacity
	// position和limit之间是未读数据, limit之后是可写区域
	int mark, position, limit, capacity;
具体实现类
	ByteBuffer  CharBuffer   ShortBuffer  IntBuffer
	LongBuffer  FloatBuffer  DoubleBuffer

/* 多路复用器 Selector */
在linux上通过epoll实现