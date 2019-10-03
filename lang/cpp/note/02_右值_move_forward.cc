# 右值，右值引用
不在当前函数栈、上层函数栈和堆上的数据，如文本段、数据段、下层函数栈的局部变量
右值的特点是不可取地址
右值引用和右值没有关系，仅指所引用的对象可以看成临时对象，其内容可被窃取过来
右值引用一定是const &(常量引用)



# 左右值重载
struct A {
  int& get() &  { return _v; }
  int  get() && { return _v; }  // 此重载的调用是右值，即*this是右值
private:
  int _v;
};



# std::move
无条件转发成右值
// impl: move.h
template<typename T>
typename remove_reference<T>::type&& move(T&& t) {
  return static_cast<typename remove_reference<T>::type&&>(t);
}
// ex: 减少拷贝
string s = "abc";
vector<string> v;
v.push_back(s);     // 调用 push_back(T&&) 重载函数
cout << s << endl;  // 空串
//
void test(int&& x) {
  int&& y = std::move(x);  // 必须用move, 否则报左值不能绑定到右值, x被看成左值
  int&& x1 = 3;
  int&& y1 = std::move(x1);  // 也必须用move
}



# std::forward
实现完美转发，左值转发成左值引用，右值转发成右值引用
move是无条件都转发成右值引用，forward则保持原特性
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