intellij idea导入tomcat-9源码, 并建立工程

1. 在tomcat源码目录下创建文件pom.xml, 内容如下:
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion><groupId>org.apache.tomcat</groupId><artifactId>Tomcat9.0</artifactId><name>Tomcat9.0 src</name>
<version>9.0</version><build><finalName>Tomcat9.0</finalName><sourceDirectory>java</sourceDirectory><resources><resource>
<directory>java</directory></resource></resources><plugins><plugin><groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-compiler-plugin</artifactId><version>2.3</version><configuration><encoding>UTF-8</encoding><source>1.8</source>
<target>1.8</target></configuration></plugin></plugins></build><dependencies><dependency><groupId>ant</groupId><artifactId>ant</artifactId>
<version>1.7.0</version></dependency><dependency><groupId>ant</groupId><artifactId>ant-apache-log4j</artifactId><version>1.6.5</version>
</dependency><dependency><groupId>ant</groupId><artifactId>ant-commons-logging</artifactId><version>1.6.5</version></dependency><dependency>
<groupId>wsdl4j</groupId><artifactId>wsdl4j</artifactId><version>1.6.2</version></dependency><dependency><groupId>javax.xml.rpc</groupId>
<artifactId>com.springsource.javax.xml.rpc</artifactId><version>1.1.0</version></dependency><dependency><groupId>org.eclipse.jdt.core.compiler</groupId>
<artifactId>ecj</artifactId><version>4.4</version></dependency><dependency><groupId>org.eclipse.jdt.core.compiler</groupId><artifactId>ecj</artifactId>
<version>4.5</version></dependency></dependencies></project>

2. Intellij idea打开tomcat源码目录: "File" --> "Open" --> 选择tomcat源码目录.

3. "ctrl+alt+shift+s"打开"Project Structure"窗口, 选择左侧的"Modules", 点"+"按钮选择"Import Module", 把源码下的"jdbc-pool"导入.

4. 在tomcat源码目录下执行"$ mvn clean compile", 编译通过.

5. "Edit configuration" --> Run/Debug Configurations窗口
   点"+"按钮选择"Application", 在"Configuration"里设置"Main Class"是"org.apache.catalina.startup.Bootstrap"

6. Run