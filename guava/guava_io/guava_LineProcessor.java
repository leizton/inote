LineProcessor<T>
	对文件逐行处理
接口
	process(String line):boolean
	getResult():T
例子
	public static void main(String[] args) throws IOException {
		// 统计文件行数
		LineProcessor<Integer> counter = new LineProcessor<Integer>() {
			private int mLineNum = 0;
			
			@Override
			public boolean process(String line) throws IOException {
				mLineNum++;
				return true;
			}
			
			@Override
			public Integer getResult() {
				return mLineNum;
			}
		}
		// 执行Files.readLines()方法
		com.google.common.io.Files.readLines(
			new java.io.File("a.txt"), Charsets.UTF_8,
			counter);
		LOGGER.info("文件行数: {}", mLineNum);
	}