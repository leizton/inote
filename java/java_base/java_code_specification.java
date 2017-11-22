1. 命名不要用缩写
2. 尽量避免null, 不要return null和传递null, 对输入要checkNotNull()
3. 项目里配xml格式化代码
4. 正确处理异常
5. 方法不要接收Stream参数, 也不要传出.
   因为接收到的Stream不知道是否已关闭, 也无法确定方法里是否要关闭.
6. Splitter.on("|")尽量用static final
7. 反射非常耗时, 用Map缓存Class对象
8. logger不用static, LoggerFactory.getLogger(getClass())
9. logger输出时不要用+, 用{}占位符
10. 可以不用正则时不用正则, 正则耗时, Pattern用static final
11. 不要出现if-elseif-else过多的条件判断, 非常不利于测试, 有时可用Map代替if
12. 变化点要用接口
13. 形成桥形的类图