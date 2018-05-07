// 用String而不是double类型来初始化BigDecimal
new BigDecimal(1.22)    // 1.219999
new BigDecimal("1.22")  // 1.22

// BigDecimal和String一样是不可变变量
d = new BigDecimal("1.22")
d.add(new BigDecimal("1.22"))  // d仍然是1.22

// 打印数组
sout(Arrays.asList(arr))

// 加载配置文件
prop = new Properties()
try (in = new FileInputStream(filename)) {
	prop.load(in)
} catch (IOException) {
}