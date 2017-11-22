/* 类名 */
Class.getName()和getCanonicalName()
例如: 内部类Foo.InnerFoo
	Foo.InnerFoo.class.getName() == xxx.Foo$InnerFoo
	Foo.InnerFoo.class.getCanonicalName() == xxx.Foo.InnerFoo
	Canonical: 规范的


/* 类型转换的安全检查 */
Class.cast(Object obj) :T {
	if obj != null && !isInstance(obj)
		throw ClassCastException
	return (T) obj
}