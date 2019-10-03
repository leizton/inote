# is_assignable<T, U>
判断是否可通过赋值运算符来转换
is_assignable<vector<int*>, vector<int*>>::value == true
is_assignable<list<int*>, list<int*>>::value     == true
is_assignable<vector<int*>, list<int*>>::value   == false

class Foo {
public:
  Foo(int) {}
  Foo& operator=(const Foo&);
};

is_assignable<Foo, int>::value == true
is_assignable<Foo, Foo>::value == true
if Foo(int)构造函数是explicit, is_assignable<Foo, int>::value==false
if operator=()是private, is_assignable<Foo, Foo>::value==false



# std::tie
获得std::tuple的各个值
// ex
double d;  int i;
std::tie(d, i) = std::make_tuple(3.14, 1);

// impl: tuple.cc
template<typename... T>
tuple<T&...> tie(T&... args) {
  return tuple<T&...>(args...);
}