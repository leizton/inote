$ grep -rn --include="*.java" "查询关键字" "查找目录"
	-r 递归
	-n 显示行号
	--include 指定查找哪些文件
	查询目录默认是当前目录

$ grep -Pn "[grep|egrep]" a.log
	-P 使用完整的正则, 默认使用基本正则

$ grep -n "\[2016-10-01\]\"GET /test.json\"" a.log
	用 \[ 和 \" 转义

$ grep -m3 "test" a.log
	-m3 返回最前面3个匹配行

$ find . -name "*.properties" | grep -i context
	查找文件名称中有context的文件
  find . -name "*.properties" | xargs grep -i handleradapter
	通过xargs变成查找文件内容
