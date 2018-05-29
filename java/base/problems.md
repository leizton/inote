1. 把ByteBuffer适配成InputStream
  实现的read()接口返回类型是int, 不能直接返回`buf.read()`
  因此是`return (buf.read() & 0xff)`, 0xff字面是int