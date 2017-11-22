读取文件
	readLines(File file, Charset charset):List<String>
	readLines(File file, Charset charset, LineProcessor<T> callback):T
	toString(File file, Charset charset):String
复制文件
	copy(File from, Charset charset, Appendable to)
	copy(File from, File to)
	copy(File from, OutputStream to)


Files
内部类
	FileByteSource
		extends ByteSource
	字段
		File file
	方法
		openStream():FileInputStream {
			return new FileInputStream()
		}
		size():long {
			if !file.isFile()
				throw new FileNotFoundException(file.toString())
			return file.length()
		}
	FileByteSink
静态方法
	asByteSource(File f) {
		return new FileByteSource(f)
	}
	asCharSource(File f, Charset cs):CharSource {
		return asByteSource(f).asCharSource(cs)
	}
	readLines(File f, Charset cs, LineProcessor<T> callback):T {
		// 调用CharSource的readLine()
		// 参考"guava_CharSource.java"
		return asCharSource(f, cs).readLines(callback)
	}