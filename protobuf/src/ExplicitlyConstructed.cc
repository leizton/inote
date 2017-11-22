template <typename T>
class ExplicitlyConstructed
> ()
    union_: union AlignedUnion {
        space:char[sizeof(T)]
        align_to_int64:int64
        align_to_ptr:void*
    }

> DefaultConstruct()
    new (&union_) T()  // 只是调用构造函数初始化，没有分配新内存
> Destruct()
    get_mutable()->~T()

> constexpr const T& get() const
    // 对于特化的T，指定constexpr可以让编译器做出优化
    return reinterpret_cast<const T&>(union_)

> T* get_mutable()
    return reinterpret_cast<T*>(&union_)