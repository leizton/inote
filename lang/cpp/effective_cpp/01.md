声明 declaration
	告诉编译器某个东西的名称和类型
	如 extern int x;  std::size_t numDigits(int num); /*函数声明*/  class Foo; /*类声明*/
定义 definition
	让编译器为变量/对象分配内存，提供函数/类的代码本体。如 int x;
初始化 initialization
	给对象赋初值，由构造函数执行

** explicit关键字
class Foo {
	private: int x;
	public:  explicit Foo(int x);
}
Foo::Foo(int x) :x(x) {}
Foo foo1(1);   // 正确
Foo foo2 = 2;  // 编译错误, explicit防止出现隐式转换

** copy-construct copy-assign
class Foo {
public:
	Foo();
	Foo(const Foo& foo);
	Foo& operator=(const Foo& foo);
}
Foo f1;       // default-construct
Foo f2(f1);   // copy-construct
f2 = f1;      // copy-assign
Foo f3 = f2;  // copy-construct，此时是定义新对象f3，因此调用的是构造函数

** 访问无效指针
int* p = NULL;
cout << *p;    // 报段错误，信号SIGSEGV

** TR1
TR1(technical report 1)是加入c++标准库的新功能, 如hash_table、smart_pointer、regular_expression
TR1组件位于std::tr1命名空间内

** lhs rhs
lhs: left hand side, 操作符左边
rhs: right hand side

** 02-用 const,enum,inline 代替 #define
例:
#define MAX(a, b) ((a) > (b) ? (a) : (b))
int a = 3, b = 0;
int c = MAX(++a, b+10);  // ++a执行1次
int d = MAX(++a, b);     // ++a执行2次
不符合直觉的是: ++a的执行次数并不总是1次，而是不确定。
从这个例子可以看成用define的危险性。

** 03-尽量用const
const char *p;  // p是常量指针(修饰词在名词前), 常量类型的指针, p所指是常量指针，p所指不可变
char* const p;  // p是指针常量, 指针类型的常量, p自身不可变
const std::vector<int>::iterator it;  // 迭代器常量, 如同const int v是整数常量
std::vector<int>::const_iterator it;  // 常量迭代器

** 04-确保对象使用前已被初始化, 注意区分初始化和赋值
例:
Person(const string &name) {
	this.name = name;  // 这是赋值, this.name在进入函数体前已经构造了
}
Person(const string &nameArg) : name(nameArg) {}  // 这是name的copy构造
类成员的初始化顺序仅与在类中声明的顺序有关，与初始化列表中的顺序无关。