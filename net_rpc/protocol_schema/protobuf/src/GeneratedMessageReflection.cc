template <typename T>
> DynamicCastToGenerated(const Message* from):T*
    // 在编译期断言类型T有default_instance()这个函数
    const T&(*get_default_instance)() = &T::default_instance
    (void) get_default_instance  // 强转成void，告诉编译期不要给出这个变量未使用的警告
    // 通过static_cast在编译期检查类型T是Message的子类
    // static_cast完成T*变量可以隐式转换成Message*的检查
    const Message* unused = static_cast<T*>(nullptr)
    (void) unused
    //
    #if defined(GOOGLE_PROTOBUF_NO_RTTI) || (defined(_MSC_VER) && !defined(_CPPRTTI))
        bool ok = &T::default_instance() ==
                  from.GetReflection().GetMessageFactory().GetPrototype(from.GetDescriptor())
        return ok ? down_cast<T*>(from) : nullptr
    #else
        // 对于上行转换(派生类转基类)，dynamic_cast和static_cast相同
        // 对于下行转换，dynamic_cast在转换失败时返回nullptr，而static_cast是编译失败
        return dynamic_cast<T*>(from)
    #endif