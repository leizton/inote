// ============================================================================ //
注解: @Resource @Autowired / @Controller @Service @Repository / @Component @Bean

ClassPathXmlApplicationContext GenericXmlApplicationContext

<bean> <constructor-arg> <property>
<context:component-scan> <context:property-placeholder>
<util:properties>
"<import>"

<mvc:interceptor>
@RequestMapping @RequestBody @JsonBody(依赖common-web)
HandlerInterceptor HandlerExceptionResolver
// ============================================================================ //


工厂模式用来代替new过程
控制反转
	依赖对象的获得被反转了. IoC的另一个名称是依赖注入.
	IoC的特点:
		使用者不需要在代码里实例化依赖项, 依赖项的生成在xml里配置.
		当要更换实现类时只需修改xml文件, 实现对象的热插拔.
		xml文件集中管理依赖配置.
		如果在IDE里修改了类名, 需要xml文件作出相应的修改, 注解方式则没有这个麻烦.


BeanFactory接口


<bean id="" class=""/>  <bean name="" class="" lazy-init="true"/> 懒加载
	对于Spring来说id和name是一样的, 在xml规范中id属性有一些限制如不能以数字开头, 所以Spring又用了name.
使用内部类, 用$分割
	<bean class="com.qunar.campus.UserServiceImpl.$.FindService"/>
实例化bean
	1. 用构造器
	2. 用类的静态工厂方法
		<bean id="clientService" class="examples.ClientServiceImpl"
			factory-method="getInstance" />
	3. 用Bean工厂类的静态方法
		<bean id="clientService" class="examples.ClientServiceImpl"
			factory-bean="examples.ClientServiceFactory"
			factory-method="create" />


构造器注入
<bean id="foo" class="Foo">
	<constructor-arg index="0" ref="user" />  <!-- 指定第几个参数 -->
	<constructor-arg type="java.lang.String" value="memo" />  <!-- 指定类型 -->
</bean>
<bean id="user" class="User">
	<constructor-arg name="username" value="zhangsan" />  <!-- 指定参数名称 -->
	<constructor-arg name="age" value="20" />
</bean>
// 当要用参数名称时, 使用注解ConstructorProperties告诉Spring构造器的每个参数的名称.
// 这是因为源代码编译后参数名称变成arg1 arg2 ..., Spring反射不出参数名称
public class User {
	@ConstructorProperties({"name", "age"})
	public User(String name, int age) {
	}
}
// 在编译时开启debug模式时, 方法的参数名称被保存到字节码的hash表中, Spring可以找到参数名称.
// 所以debug编译时, 不用加ConstructorProperties注解.


setter注入
<bean id="user" class="User">
	<property name="username" value="zhangsan" />
	<property name="age" value="20" />
</bean>


构造器注入的字段是必选, setter注入的字段是可选.
构造器注入的一个好处是可把字段设成final, 这样就是线程安全的.


Bean的Scope
	singleton  一个容器里只有一个实例, 该实例可能涉及线程安全
	prototype  每次调用getBean()时返回新的实例
	request(mvc里一次请求)  session

使用ApplicationContext, 不用BeanFactory
	ClassPathXmlApplicationContext
	FileSystemXmlApplicationContext


Autowiring 自动装配
1. no      默认不用autowiring
2. byName  找到bean-id(或bean-name)和字段名匹配的bean注入, 没有相关bean时设成null
3. byType  容器里只有一个与字段的类型匹配的bean时注入, 如果有多个匹配上那么抛出异常
4. constructor
现在不用autowiring属性, 因为装配结果存在不确定性.


<context:property-placeholder />


<context:annotation-config />
	在实例化bean时, 找到这些bean中有 @Resource 注解的字段, 这些字段进行autowiring,
	autowiring先匹配名字, 再匹配类型.


<context:component-scan base-package="com.examples" />
	使 @Component 生效, @Component 注解的类Spring会为之生成bean.
	该标签默认包含<context:annotation-config />, 因此 @Component 和 @Resource 都生效
需要扫描多个包时, 用多个<context:component-scan>标签, 注意: 此时base-package里不能用*, 只能写包名.


@Service     和 @Component 作用相同, 但包含一个语义: 这个类在Service层.
@Repository  包含语义: 这个类在持久层.
@Controller  包含语义: 这个类SpringMvc的Controller.


@Bean 和 @Component 的区别:
	@Bean 作用于方法, 其所在的类需要加 @Configuration 注解.
	@Component 作用于类.


Spring的回调
1. InitializingBean: afterPropertiesSet()
   DisposableBean: destory()
2. <bean class="" init-method="init" destory-method="destory" />
3. 注解 @PostConstruct @PreDestory
4. ApplicationContextAware: setApplicationContext(ApplicationContext)
      用于获取上下文
   BeanNameAware: setBeanName(String)
      用于获取<bean>的name属性