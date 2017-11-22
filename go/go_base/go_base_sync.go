// 锁接口
type Locker interface {
	Lock()
	Unlock()
}

// 互斥锁
sync.Mutex
func main() {
	var mutex sync.Mutex
	go func() {
		println("Alice to lock")
		mutex.Lock()
		println("Alice get lock")
		for i := 1; i <= 3; i++ {
			time.Sleep(time.Second)
			println("Alice has sleeped ", i)
		}
		println("Alice to unlock")
		mutex.Unlock()
	}()
	time.Sleep(time.Second)
	println("Bob to lock")
	mutex.Lock()
	println("Bob get lock")
	println("Bob to unlock")
	mutex.Unlock()
}

// 读写锁
sync.RWMutex
写锁: func (*RWMutex) Lock;  func (*RWMutex) Unlock;
读锁: func (*RWMutex) RLock; func (*RWMutex) RUnlock;

// 条件变量
等待通知, 检查条件
sync.Cond
用锁创建: func NewCond(l Locker) *Cond
方法: Wait Signal Broadcase

// 原子操作
import ( "sync/atomic" )
>> 原子增减
var i32 int32
atomic.AddInt32(&i32, 3)   // 不做初始化时, i32是0
atomic.AddInt32(&i32, -5)  // 减法, 与负数补码的加法相当于对绝对值的减法
atomic.AddInt64(&i64, 3)   // 64位整数
对于不能取地址的数组，无法做原子操作
newi32 := atomic.AddInt32(&i32, 3)  // 返回新值的目的是避免读取时i32又改变了
>> 比较交换CAS
var isChange bool
isChange = atomic.CompareAndSetInt32(&i32, v, (v+delta))
>> 原子地读取
在32位机上读一个int64变量，可能读时有一个并发写且只写了一半，此时读到的值有问题
for {
	v := atomic.LoadInt32(&value)
	if atomic.CompareAndSetInt32(&value, v, (v+delta)) {
		break
	}
}
>> 原子地写入
意义: 如在32位机上并发写一个int64变量，防止出现写错误
atomic.StoreInt32(&value, v)
>> 原子地交换
atomic.SwapInt32(&i, &j)

// 只调用1次
var onceDo sync.Once
once.Do( func() {println("once do")} )
once.Do()的输入参数是无参数无结果的函数
//
func main() {
	const callNum = 3
	sign := make(chan bool, callNum)
	var num int32
	generateFunc := func(i int32) func() {
		return func() {
			atomic.AddInt32(&num, i)
			sign <- true
		}
	}
	var onceDo sync.Once
	for i := 1; i <= callNum; i++ {
		onceDo.Do( generateFunc(int32(i)) )
	}
	// 输出1次receiveSign, 2次timeout
	for i := 0; i < callNum; i++ {
		select {
			case <- sign:
				println("receiveSign")
			case <- time.After(time.Second):
				println("timeout")
		}
	}
	println(num)  // num == 1
}
虽然传给sync.Once.Do()多次相同或不同的函数，但只有某个被执行

// WaitGroup 即java的CountLatchDown
var wg sync.WaitGroup
wg.Add(3)   // 内部计数器变成3
wg.Done()   // 计数器减1
wg.Add(-2)  // 计数器减2
wg.Wait()   // 等待计数器归0
wg的内部计数器变成负数时，引发panic

// 临时对象池
池中最多只有一个元素
当池是空的，且New字段是nil，调用Get()方法得到nil
每个使用池的Goroutine相关联的P都有一个本地池，当本地池空时会尝试从其他P获取
func main() {
	// debug.SetGCPercent(-1)禁用GC并返回之前的设置
	// 通过defer在main()执行完成后恢复GC
	// debug.SetGCPercent(-1)先被求值
	defer debug.SetGCPercent(debug.SetGCPercent(-1))
	//
	var obj int32
	newFunc := func() interface{} {
		return atomic.AddInt32(&obj, 1)
	}
	pool := sync.Pool { New: newFunc }
	//
	v1 := pool.Get()  // 调用newFunc(), 返回1
	pool.Put(2)       // 放入2
	v2 := pool.Get()  // 取出刚刚Put放入的2
	pool.Put(3)       // 已经有元素, 放入失败
	pool.Put(4)       // 放入4
	v3 := pool.Get()  // 取出放入的4
	//
	debug.SetGCPercent(100)  // 清理对象池
	runtime.GC()
	v4 := pool.Get()  // 调用newFunc(), 返回2
	pool.New = nil    // 把new对象函数置空
	v5 := pool.Get()  // 得到nil
	// 1 2 4 2 nil
	fmt.Printf("%v, %v, %v, %v, %v\n", v1, v2, v3, v4, v5)
}