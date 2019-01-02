# gcc编译参数
# 使用 "\_\_FILE\_\_" 和 "\_\_LINE\_\_" 打印错误信息
"\_\_FILE\_\_" 和 "\_\_LINE\_\_" 在预编译阶段被替换成代码文件名和行号


# 获取结构体中字段的偏移
```c++
#define OFFSET(STRUCT,FIELD) reinterpret_cast<uint64_t>(reinterpret_cast<void*>(&reinterpret_cast<STRUCT*>(0)->FIELD))
```


# constexpr
用constexpr修饰函数，可以实现让编译器在编译期判断该函数返回的值是否是常量，
如果是常量，则可以让编译器做一定的优化。


# <unistd.h>
包含了POSIX操作系统的api函数的声明


# STDIN_FILENO stdin
STDIN_FILE 是<unistd.h>里定义的宏, 类型是int
stdin 类型是FILE*


# 可变参数
c函数参数是从右往左入栈, 所以可变参数先于固定参数入栈, 可变参数在高地址
最后一个固定参数的下一个栈底元素就是第一个可变参数, 这是va_start()的实现原理
```c++
typedef void* va_list;

//sizeof(n)/sizeof(int)向上取整乘sizeof(int)
#define _INTSIZEOF(n)   ((sizeof(n) + sizeof(int) - 1) & ~(sizeof(int) - 1));

#define va_start(ap,v)  (ap = (va_list)&v + _INTSIZEOF(v))

//获得ap的原值并使ap往后移_INTSIZEOF(type)
#define va_arg(ap,type) (*(type*)( (ap+=_INTSIZEOF(type)) - _INTSIZEOF(type) ))

#define va_end(ap)      (ap = (va_list)0)
```