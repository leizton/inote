# 读取用户输入
```sh
# a.sh
echo -e "input name:\c"  # -e表示启用转义字符
read name                # 用户按回车结束
echo -e "input password:\c"
read password
echo "{name=$name, pwd=$password}"
```

# 当shell中执行某个有输入yes类似交互的命令时，可以用重定向输入来解决
例如：
```sh
$ echo -e "abc\n123" > a.input
$ /bin/bash a.sh < a.input
```