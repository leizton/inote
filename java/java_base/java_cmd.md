# 不指定package
```java
public class Main {
  public static void main(String[] args) {
    var l = new java.util.ArrayList<String>(); l.add("hello"); System.out.println(l);
  }
}
```
```shell
$ javac Main.java  # 生成Main.class
$ java Main        # 找到类名是Main的类, 运行其静态main()方法
```

# 指定package
```java
package com.wh.learn.jdk10;
public class Main {
  public static void main(String[] args) {
    var l = new java.util.ArrayList<String>(); l.add("hello"); System.out.println(l);
  }
}
```
```shell
$ javac -d target src/com/wh/learn/jdk10/Main.java  # 生成target/com/wh/learn/jdk10/Main.class
$ java -cp target 'com.wh.learn.jdk10.Main'         # 此时类名需要带上包名
```