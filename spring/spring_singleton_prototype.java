有如下配置

@Component(value = "mybean")
public class Test {
	private String v;
	public Test() { this.v = "hello-annotation"; }
	public Test(String v) { this.v = v; }
	@PostConstruct
	public void init() { System.out.println(v); }
}

>> 情况1
// root.xml
<import resource="a.xml"/>
<import resource="b.xml"/>
// a.xml
<bean id="mybean" class="com.test.Test">
	<construct-arg name="v" value="hello-a.xml"/>
</bean>
// b.xml
<bean id="mybean" class="com.test.Test">
	<construct-arg name="v" value="hello-b.xml"/>
</bean>
<context:component-scan base-package="com.test"/>
// 输出: hello-a.xml
// b.xml定义的bean会被忽略, 而不是覆盖a.xml的定义
// 默认是scope="singleton", 所以@Component并不触发bean的创建

>> 情况2
把@Component(value = "mybean") 变成 @Component()
输出:
	hello-a.xml
	hello-annotation
当没有id约束时创建了2个bean