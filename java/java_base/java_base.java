transient 关键字, 关闭序列化


被抛出的异常是不可能发生的错误, 当前函数没有处理措施所以抛出
被try-catch的异常是可处理或可容忍的错误


静态内部类, 只有内部类可以用static修饰
	static class 表示它不需要访问外部类的this
	普通内部类可以用 "外部类名.this" 访问


java的4种引用
	Strong  Reference 强引用, 普通的对象引用 Object obj = new Object(), 当Root Set不可达时GC回收
	Soft    Reference 软引用, 生命周期仅次于Strong Reference, 当Heap满时GC回收, 可用于不限大小的cache(缓存)
	Weak    Reference 弱引用, 当它包装的ref只被Weak Reference自身引用时, GC回收
	Phanton Reference 虚引用, 当它包装的ref将要被GC回收时, 会被放入一个ReferenceQueue对象, 其他处代码调用
ReferenceQueue对象的remove()阻塞方法获取PhantonReference对象, 从而感知ref被回收事件.
参考: https://github.com/whiker/learnjava/blob/master/javabase/src/test/java/com/whiker/learn/javabase/ReferenceTest.java


/* 重写equals */
@Override
public boolean equals(Object obj) {
	if (!super.equals(obj)) {  // 包含了obj非null的检查
		return false;
	}
	if (obj == this) {
		return true;
	}
	if (!(obj instanceof Foo)) {
		return false;
	}
	Foo foo = (Foo) obj;
	// compare fields
}
java.lang.Object::equals(Object obj) {
	return this == obj;
}


/* clone */
// clone可能比new慢
@Override
public Object clone() {
	try {
		/**
		 *  这里是浅copy, 对于int[]等字段也只是复制引用
		 */
		return super.clone(); // native方法
	} catch (CloneNotSupportedException e) {
		return new Foo(this.field);
	}
}


/* String.format() */
String s = "abcdefghi_", s1;
for (int i = 0; i < 10000000; i++) {
	s1 = String.format("%s%d", s, i); // 用时 9857 ms
	s1 = s + i; // 用时 602 ms
	s1 = String.format("%f", new Float(i)); // 用时 10457 ms
	s1 = Float.toString(i); // 用时 932 ms
	// format包括字符串解析, 类型判断所以更慢
}


/* 取余 还是 if */
int n = 10, i = 0;
for (int c = 0; c < 100000000; c++) { // 1亿次
	if (++i == n) i = 0; // 用时 68  ms
	i = (i + 1) % n;     // 用时 762 ms, 每次都做取余操作
	i = (i + 1) % 10;    // 用时 389 ms
}


/* Integer 和 Float 的 toString */
// Integer的toString比Float快
float a = 0.123456;
for (int i = 0; i < 10000000; i++) {
	String s = Float.toString(a); // 用时 1472 ms
	String s = new Integer((int) (a * 1000000)).toString(); // 用时 483 ms
}


输出文件的换行不要硬编码成\n或\r\n, 有如下方法:
	1. writer.write(System.getProperty("line.separator"));
	2. writer.write(StandardSystemProperty.LINE_SEPARATOR.value());
	3. BufferedWriter的newline()
		实际是 write(lineSeparator)
		lineSeparator = java.security.AccessController.doPrivileged(
			new sun.security.action.GetPropertyAction("line.separator"));


先把接口写出来, 在写具体实现代码


String中的intern方法
   s.intern(), 当s在池中有与之equals()成立的String对象时返回静态池中String对象, 否则会创建新的String对象.
   静态池位于Perm区(永久代), Perm区内存小, 过多intern的String会出现OOM(out of memory)
   如下:
   public static void main(String[] args) {
       String s1 = "a";
       String s2 = new String("a");
       String s3 = new String("a").intern();
       s1 == s2;  // false
       s1 == s3;  // true
       s2 == s3;  // false
   }


泛型的<? super T> <? extends T>, super声明下界, extends声明上界


$ javac Main.java  把Main.java编译成Main.class
$ javap -c Main.class  反汇编分析Main.class, 部分指令如下:
	load   变量值入栈, 入栈即写入栈顶, 栈是函数栈/线程的工作内存/线程的独立内存区域
	store  栈顶值写入局部变量, 如istore_0是把int型值写到第0个局部变量
	new    创建对象, 并入栈
	getstatic  类的静态字段入栈
	putstatic  对静态字段赋值
	getfield   对象的字段入栈
	putfield   对对象的字段赋值
	add/sub/mul/div  加/减/乘/除后, 结果入栈
	invokevirtual   调用对象的方法
	invokespecial   调用超类的构造方法/对象的初始化方法/对象的私有方法
	invokestatic    调用类的静态方法
	invokeinterface  调用接口方法


volatile, 跨越内存栅栏, 线程有自己的cache(如cpu三级缓存), 所以可能与主存不一致.
	volatile变量被修改后, 其新值被写回主存, 并通知其他工作内存中的值不是最新.
	如果通知不即使, 也可能用了旧的值, 但下一次再用时就是新的.
1. 保证读取时是主存的最新数据. 例子:
	volatile boolean stop = false;
	// 线程-1
	while (!stop) {
		...
	}
	// 线程-2
	stop = true;
	如果stop不用volatile, 则有2个问题:
		线程-1可能较长时间不查看主存里的stop是否有变化; 线程-2修改stop后只是改变cache, 主存没有改变;
	volatile让线程-2对stop的改变必须写到主存中, 此时线程-1会知道cache里的stop失效而从主存读取新值.
2. volatile的另一个作用是阻止jvm指令重排序, 例如
	// 线程-2
	lock ... unlock
	stop = true;
	编译后的指令可能是先进行stop=true再进行lock...unlock, 因为把stop设成true不影响加锁代码块的执行,
	问题是线程-2的本意是执行完加锁代码块再让线程-1去stop, volatile可以阻止这种重排序.


锁具有可见性, 即用了锁表示也用了volatile
	例如 synchronized { i++; }
	线程-1修改了i, 为了让线程-2在此基础上修改i, 则线程-1必须写回主存, 线程-2再从主存中读取.


positions:
for (int i = 0; i < 2; i++) {
    for (int j = 0; j < 10; j++) {
        for (int k = 5; k < 10; k++) {
            if (k == 7) {
                continue positions;  // 跳到最外层for上, 继续最外层的for循环
            }
            System.out.println(String.format("%d %d %d", i, j, k));
        }
    }
}
输出, 注意j到不了1, k到不了8
0 0 5
0 0 6
1 0 5
1 0 6