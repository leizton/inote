> 在 "META-INF/spring.schemas" 指定：
http\://code.alibabatech.com/schema/dubbo/dubbo.xsd=META-INF/dubbo.xsd
发布dubbo自己的xsd文件，dubbo.xsd也在 META-INF 目录下。

> 在 "META-INF/spring.handlers" 指定：
http\://code.alibabatech.com/schema/dubbo=com.alibaba.dubbo.config.spring.schema.DubboNamespaceHandler
注册解析xml配置实现类的是: DubboNamespaceHandler
解析实现类是: DubboBeanDefinitionParser
DubboBeanDefinitionParser-实现了spring的-BeanDefinitionParser-接口


public class DubboNamespaceHandler extends NamespaceHandlerSupport {
	static {
		Version.checkDuplicate(DubboNamespaceHandler.class);
	}

	@Override  // NamespaceHandlerSupport
	public void init() {
	    registerBeanDefinitionParser("application", new DubboBeanDefinitionParser(ApplicationConfig.class, true));
        registerBeanDefinitionParser("module", new DubboBeanDefinitionParser(ModuleConfig.class, true));

        // <dubbo:registry>，定义注册
        registerBeanDefinitionParser("registry", new DubboBeanDefinitionParser(RegistryConfig.class, true));

        registerBeanDefinitionParser("monitor", new DubboBeanDefinitionParser(MonitorConfig.class, true));
        registerBeanDefinitionParser("provider", new DubboBeanDefinitionParser(ProviderConfig.class, true));
        registerBeanDefinitionParser("consumer", new DubboBeanDefinitionParser(ConsumerConfig.class, true));
        registerBeanDefinitionParser("protocol", new DubboBeanDefinitionParser(ProtocolConfig.class, true));

        // <dubbo:service>，定义provider端的RPC接口实现bean
        registerBeanDefinitionParser("service", new DubboBeanDefinitionParser(ServiceBean.class, true));

        // <dubbo:reference>，定义consumer端的引用bean
        registerBeanDefinitionParser("reference", new DubboBeanDefinitionParser(ReferenceBean.class, false));

        // <dubbo:annotation>
        registerBeanDefinitionParser("annotation", new DubboBeanDefinitionParser(AnnotationBean.class, true));
    }
}