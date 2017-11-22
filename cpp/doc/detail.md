# 使用 "\_\_FILE\_\_" 和 "\_\_LINE\_\_" 打印错误信息
"\_\_FILE\_\_" 和 "\_\_LINE\_\_" 在预编译阶段被替换成代码文件名和行号

# 获取结构体中字段的偏移
```c++
#define OFFSET(STRUCT,FIELD) reinterpret_cast<uint64_t>(reinterpret_cast<void*>(&reinterpret_cast<STRUCT*>(0)->FIELD))
```

# constexpr
用constexpr修饰函数，可以实现让编译器在编译期判断该函数返回的值是否是常量，
如果是常量，则可以让编译器做一定的优化。