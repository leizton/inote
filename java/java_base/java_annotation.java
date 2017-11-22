Java元注解
	对注解进行注解, Java5定义了4个元注解
	1. @Target
		定义Annotation所修饰的对象类型
		@Target(ElementType.CONSTRUCTOR) 可用在构造器上
		还有: FIELD 字段, METHOD 方法, PARAMETER 方法的形参, PACKAGE 包,
			  LOCAL_VARIABLE 局部变量, TYPE 类/接口/enum, ANNOTATION_TYPE 注解, and-so-on.
		"Target.java"的代码
		@Documented
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.ANNOTATION_TYPE)  // 自己作用于自己, 编译器不会死循环
											  // 用了ANNOTATION_TYPE表明这是个元注解
		public @interface Target {
			ElementType[] value();  // 可以在@Target()里写多个修饰类型
		}
	2. @Retention
		定义Annotation保留到的时间
		@Retention(RetentionPolicy.RUNTIME) 保留到运行期
		还有: SOURCE 保留到源代码文件 如@Override的Retention是RetentionPolicy.SOURCE,
			  CLASS 保留到class文件
	3. @Documented
		默认javadoc是不包括Annotation, @Documented表明该Annotation应该被javadoc记录.
	4. @Inherited
		表示Annotation可以作用到其修饰类的子类上.
		所以当在某个类上查不到Annotation时, 如果这个Annotation被@Inherited过,
		那么可以在super-class上查找.

Meta-Data/Annotation
	元数据, 描述数据的数据, 如注解
	注解分成3种:
		1. 给开发者看的, 如@Deprecated修饰的方法被弃用, 使用者不能再调用该方法了.
		2. 给编译器看的, 如@Override告诉编译器该方法是覆盖父类方法, 参数和返回类型应和父类方法相同.
		3. 给第三方框架看的, 如第三方提供了一个工具: ToStringUtil(Object), 用于将对象转String,
			此时第三方还定义了注解@ToStringIgnore, 作用在字段(ElementType.Field)上,
			表示该字段在ToStringUtil()中被忽略.