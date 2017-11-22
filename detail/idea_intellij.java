File - Settings - 搜索"Antialiasing" - 选"Greyscale"


查看类代码(.java文件)的所有方法和字段
	View -> ToolWindows -> Structure (Alt+7)


ctrl + /                单行注释
ctrl + shift + /        多行注释
ctrl + d                复制当前行
ctrl + x                剪切当前行, 也可用于删除
ctrl + shift + t        创建单元测试

alt + 7                 显示"Structure"栏, 再按隐藏
alt + 1                 显示"Project"栏, 再按隐藏
alt + Insert            自动补全代码
ctrl + i                补全未实现的接口
ctrl + o                补全可Override的方法

shift + F6              重命名, Refactor

ctrl + alt + shift + s  打开"Project Structure"窗口

alt + 左右箭头          左右切换代码视图
alt + 上下箭头          切换上下一个类方法

ctrl + q                显示注释文档
ctrl + p                显示类方法的参数提示

ctrl + n                查找类
ctrl + shift + n        查找文件
ctrl + alt + shift + n  查找类中的方法/变量
ctrl + shift + f        在工程目录的所有文件里查找, 如已知dao的某个接口名查*-mapper.xml文件时用到

ctrl + b                goto declaration, 右键"Go to"跳至定义处


在pom.xml的<dependency>上右键, 选Maven, 有Download Sources


把含有源码的目录"Make Directory As"成"Sources Root"后, 用"ctrl+n"可以搜索到其中的类.


去掉MyBatis的"*Mapper.xml"的警告: 在sql语句上按"Alt+Enter"选"Un-inject Language/Reference"


使用"javax.tools.JavaCompiler"时, 出现"Usage of API documented as @since 1.6"红线
解决: Settings | Editor | Inspections | Java language level migration aids | Usages of API ... 取消勾选


注释模板, Settings | Editor | File and Code Templates


调试
F7 进入方法
F8 单步执行
F9 跳到下一个断点

Settings | Editor | General | Appearance