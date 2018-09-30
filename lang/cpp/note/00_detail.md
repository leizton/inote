# 关键字 操作符
- __thread
  gcc关键字，p643
- constexpr
- decltype
  获取表达式的类型, 和sizeof一样在编译期确定

# 容器
- vector::push_back()
  扩容时调用copy-constructor, 依赖std::copy()或std::_Construct()

# const member function
类的const成员函数不能调用non-const成员函数

# 自动推断函数的返回类型
```cpp
double add(double x, double y) { return x + y; }
template<typename T> auto tri(T x, T y) -> decltype(add(x, y)) { return 3 * add(x, y); }
int main() { cout << tri(1, 2); }
```