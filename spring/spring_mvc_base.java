web.xml
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>/WEB-INF/config/spring/spring-*.xml</param-value>
	</context-param>

	<listener>
		<listener-class> org.springframework.web.context.ContextLoaderListener </listener-class>
	</listener>


<web-app>
    <!-- servlet -->
    <servlet>
        <servlet-name>springmvc</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    </servlet>
    <!-- servlet-mapping -->
    <servlet-mapping>
        <servlet-name>springmvc</servlet-name>
        <url-pattern>*.do</url-pattern>
    </servlet-mapping>
    <!-- filter -->
    <filter>
        <filter-name>encodingFilter</filter-name>
        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
        <init-param>
            <param-name>forceEncoding</param-name>
            <param-value>true</param-value>
        </init-param>
    </filter>
    <!-- filter-mapping -->
    <filter-mapping>
        <filter-name>encodingFilter</filter-name>
        <url-pattern>"/*"</url-pattern>
    </filter-mapping>
</web-app>


@RequestMapping
value = "a.do"  响应url
consumes = "application/json"
	只处理"Content-type"包含"application/json"的请求, 如"application/json"或"application/*"
produces = "application/json"
	只处理"Accept"包含"application/json"的请求, 一般chrome设置成,
	Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*;q=0.8