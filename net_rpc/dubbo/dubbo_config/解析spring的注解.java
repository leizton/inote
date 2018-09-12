AnnotationBean
	implements BeanPostProcessor
// 解析@Service
> postProcessAfterInitialization(Object bean, String beanName):Object @Override-BeanPostProcessor
	Service service = bean.getClass().getAnnotation(Service.class)
	if service == null  // 没有@Service注解
		return bean
	// 由注解的配置解析出ServiceBean
	ServiceBean<Object> serviceConfig = new ServiceBean<>(service)
	...
// 解析@Reference
> postProcessBeforeInitialization(Object bean, String beanName):Object @Override-BeanPostProcessor
	// 方法上的注解
	Method[] methods = bean.getClass().getMethods()
	for Method method : methods
		if !(method.name.length > 3 && method.name.startWith("set") && ...)
			continue
		Reference reference = method.getAnnotation(Reference.class)
		if reference != null
			Object value = this.refer(reference, method.getParameterTypes()[0])
			if value != null
				method.invoke(bean, value)  // 调用bean的setter
	// 字段上的注解
	Field[] fields = bean.getClass().getDeclaredFields()
	for Field field : fields
		Reference reference = method.getAnnotation(Reference.class)
		if reference != null
			Object value = this.refer(reference, field.getType())
			if value != null
				field.set(bean, value)  // 设置字段的值
> refer(Reference reference, Class<?> referenceClass):Object
	// 由注解的配置解析出ReferenceBean
	ReferenceBean<?> referenceConfig = ...
	return referenceConfig