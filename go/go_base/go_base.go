/* 引用类型的值传递和地址传递 */
type MyIntSlice []int
func (self MyIntSlice) set(i, v int) {  // 参数类型是 MyIntSlice
	self[i] = v
}
func (self *MyIntSlice) append(v int) {  // 参数类型是 *MyIntSlice
	*self = append(*self, v)
}
func (self *MyIntSlice) set1(i, v int) {  // 参数类型是 MyIntSlice
	(*self)[i] = v  // 必须用()包裹*self
}
func main() {
	var nums MyIntSlice
	nums.append(1); nums.append(2); fmt.Println(nums)  // [1, 2]
	nums.set(1, -1); fmt.Println(nums)  // [1, -1]
}

/* slice引用数组时，小心slice可能不再指向数组 */
func main() {
	a := [5]int{1, 2, 3, 4, 5}
	s := a[1:4]        // a=[1  2 3 4  5], s=[2  3 4]
	s = append(s, -1)  // a=[1  2 3 4 -1], s=[2  3 4 -1]
	s[0] = 31          // a=[1 31 3 4 -1], s=[31 3 4 -1]

	// s的容量不够append时，底层创建了新数组
	// !BeCareful: s和a不再关联
	s = append(s, -2)  // a=[1 31 3 4 -1], s=[31 3 4 -1 -2]
	s[0] = 51          // a=[1 31 3 4 -1], s=[51 3 4 -1 -2]
}

/* []byte和string间的转换 */
func bytesToString(bs []byte) string {
	return *(*string)(unsafe.Pointer(&bs))
}
func stringToBytes(s string) []byte {
	return *(*[]byte)(unsafe.Pointer(&s))
}

/* defer和return的先后 */
func aid(s string) int {
	println(s)
	return 0
}
func test() int {
	defer aid("defer")
	return aid("return")
}
func main() {
	test()  // 先return, 再defer
}

/* defer中捕获错误 */
func aid() {
	_, err := os.Open("/etc/abc")
	if err != nil {
		panic(err)
	}
}
func test() {
	defer func() {
		if r := recover(); r != nil {
			if pe, ok := r.(*os.PathError); ok {
				fmt.Printf("操作:%s, 路径:%s, msg:%s\n", pe.Op, pe.Path, pe.Err)
			}
		}
	}()
	// defer必须在aid()前面
	aid()
}
func main() {
	test()
}

/* debug.SetGCPercent() */
debug.SetGCPercent(percent)
当新分配的数据占上一次GC后存活数据的百分之percent时，触发GC