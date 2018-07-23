# transient
关闭序列化

# static内部类
不需要访问外部类的this
non-static内部类可以用`外部类名.this`

# 4种引用
Strong  强引用, 普通的对象引用 Object obj = new Object(), 当Root Set不可达时GC回收
Soft    软引用, 生命周期仅次于Strong Reference, 当Heap满时GC回收, 可用于不限大小的cache(缓存)
Weak    弱引用, 当它包装的ref只被Weak Reference自身引用时, GC回收
Phanton 虚引用, 当它包装的ref将要被GC回收时, 会被放入一个ReferenceQueue对象, 其他处代码调用
ReferenceQueue对象的remove()阻塞方法获取PhantonReference对象, 从而感知ref被回收事件

# 反射非常耗时, 用Map缓存Class对象

# 泛型的<? super T> <? extends T>
super声明下界, extends声明上界

# volatile
跨越内存栅栏, 线程有自己的cache(如cpu三级缓存), 所以可能与主存不一致
volatile变量被修改后, 其新值被写回主存, 并通知其他工作内存中的值不是最新
如果通知不及时, 也可能用了旧的值, 但下一次再用时就是新的
1. 保证读取时是主存的最新数据. 例子:
	volatile boolean stop = false;
	// 线程-1
	while !stop
		...
	// 线程-2
	stop = true
	如果stop不用volatile, 则有问题:
		线程-1可能较长时间不查看主存里的stop是否有变化; 线程-2修改stop后只是改变cache, 主存没有改变
2. volatile的另一个作用是阻止jvm指令重排序, 例如
	// 线程-2
	lock ... unlock
	stop = true
	编译后的指令可能是先进行stop=true再进行lock...unlock, 因为把stop设成true不影响加锁代码块的执行,
	问题是线程-2的本意是执行完加锁代码块再让线程-1去stop, volatile可以阻止这种重排序.

# 锁包含volatile语义

# POJO PO VO BO DTO
- model
  POJO  plain ordinary java object    符合JavaBean规范的普通对象
- 持久层
  PO    persistant object      持久对象，对应数据库表的一行记录
- service层
  VO    value object           值对象，业务层内传递的数据，包含PO
  BO    business object        业务对象，领域模型中的领域对象
- controller层
  DTO   data transfer object   数据传输对象，controller与外部交互的对象，
                               从service层获取的po、vo无需全部返回给调用者，这时可以转成dto

# javac
- 不指定package
public class Main {
  public static void main(String[] args) {
    var l = new java.util.ArrayList<String>(); l.add("hello"); System.out.println(l);
  }
}
$ javac Main.java  // 生成Main.class
$ java Main        // 找到类名是Main的类, 运行其静态main()方法
- 指定package
package com.wh.learn.jdk10;
$ javac -d target src/com/wh/learn/jdk10/Main.java  // 生成target/com/wh/learn/jdk10/Main.class
$ java -cp target 'com.wh.learn.jdk10.Main'         // 此时类名需要带上包名

# 常用调试命令
- jstat
jstat -gc <pid> 2000
- jstack
jstack <pid> > <outfile>
- jmap, jhap
jmap -histo:live <pid> > <resultfile>  // 直方图, 加live选项过滤出'live objects'
jmap -dump:live,file=<outfile> <pid>
jhat <outfile>
$JAVA_HOME/bin/jvisualvm
- jps
查看当前java进程