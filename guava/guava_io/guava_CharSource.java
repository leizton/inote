CharSource
方法
	// 打开流
	abstract openStream():Reader
	openBufferedStream():BufferedReader {
		Reader r = openStream()
		reutrn (r instanceOf BufferedReader)
				? r : new BufferedReader(r)
	}
	// 计算字符数(不是字节数)
	countBySkipping(Reader r) {
		long count = 0
		// skip()跳过字符
		while readNum = r.skip(Long.MAX_VALUE), readNum != 0
			count += readNum
		return count
	}
	lengthIfKnow():Optional<Long> {
		return Optional.absent()
	}
	length():long {
		Optional<Long> lengthIfKnow = lengthIfKnow()
		if lengthIfKnow.isPresent()
			return lengthIfKnow.get()
		// 关闭字符流
		Closer closer = Closer.create()
		try
			Reader r = closer.register( openStream() )
			return countBySkipping(r)
		catch Throwable e
			throw closer.rethrow(e)
		finally
			closer.close()
	}
	// 读取
	read():String {
		Reader r = closer.register( openStream() )
		return CharStreams.toString(r)  // 使用了java.nio.CharBuffer
	}
	readLines():ImmutableList<String>
		使用BufferedReader和Lists.newArrayList()
	readLines(LineProcessor<T> proc)
		/* CharStreams.readLines() 依赖 LineReader
		 * LineReader 依赖 LineBuffer */
		return CharStreams.readLines(reader, proc)

LineReader
字段
	char[] buf = new char[0x1000]  // pow(2, 12), 4k个字符
	CharBuffer cbuf = CharBuffer.wrap(buf)
	
	Queue<String> lines = new LinkedList()
	LineBuffer lineBuf = new LineBuffer() {
		@Override # handleLine(String line, String end) {
			lines.add(line)
		}
	}
方法
	readLine():String {
		while lines.peek() == null  // 只获取队头元素的引用, 不出队
			cbuf.clear()
			int nRead = (reader != null)
						? reader.read(buf, 0, buf.length)
						: readable.read(cbuf)
			if nRead == -1
				lineBuf.finish()
				break
			lineBuf.add(buf, 0, nRead)
		return lines.poll()  // 会出队
	}
	/* LineBuffer.add()在读到buf中的'\n'时, 调用LineBuffer.finishLine().
	 * finishLine()调用LineBuffer.handleLine(line, end), end是"\r\n"或"\n".
	 * LineBuffer.finish()调用finishLine(), 从而触发handleLine().
	 */