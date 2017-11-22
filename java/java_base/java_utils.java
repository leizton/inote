// 数组copy
String[] arr1 = new String[] { "a1", "a2", "a3" };
String[] arr2 = new String[2];
System.arraycopy(arr1, 1, arr2, 0, 2);
System.out.println(Arrays.asList(arr2)); // [a2, a3]

// 数组转List
int[] arr = new int[] { 1, 2 }
Arrays.asList(arr) 返回类型 List<int[]>
Integer[] arr = new Integer[] { 1, 2 }
Arrays.asList(arr) 返回类型 List<Integer>

// 加载配置文件
Properties props = new Properties()
try (InputStream in = new FileInputStream(filename)) {
	props.load(in)
} catch (IOException e) {
}