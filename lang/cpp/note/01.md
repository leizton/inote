# 右值，右值引用
不在当前函数栈、上层函数栈和堆上的数据，如文本段、数据段、下层函数栈的局部变量
右值的特点是不可取地址
右值引用和右值没有关系，仅指所引用的对象可以看成临时对象，其内容可被窃取过来
右值引用一定是const &(常量引用)

# 左右值重载
```c++
struct A {
  int& get() &  { return _v; }
  int  get() && { return _v; }  // 此重载的调用是右值，即*this是右值
private:
  int _v;
};
```

# std::move
无条件转发成右值
```c++
// impl: move.h
template<typename T>
typename remove_reference<T>::type&& move(T&& t) {
  return static_cast<typename remove_reference<T>::type&&>(t);
}
```

# std::forward
实现完美转发，左值转发成左值引用，右值转发成右值引用
move是无条件都转发成右值引用，forward则保持原特性
```c++
// ex
struct A {
  A(int& i)  { cout << "lvalue" << endl; }
  A(int&& i) { cout << "rvalue" << enld; }
};

template<class T, class... U>
unique_ptr<T> create(U&... u) {
  return unique_ptr<T>(new T( std::forward<U>(u)... ));
}

auto p1 = create<A>(1);  // rvalue
int i = 0;
auto p2 = create<A>(i);  // lvalue
```

# std::tie
获得std::tuple的各个值
```c++
// ex
double d;  int i;
std::tie(d, i) = std::make_tuple(3.14, 1);

// impl: tuple.cc
template<typename... T>
tuple<T&...> tie(T&... args) {
  return tuple<T&...>(args...);
}
```