cd        -          返回最近一次cd前的目录
tree      -L 2       最多显示2层
which     exe        查找可执行文件位置
whereis   file       查找可执行文件和man-page
ln        -s         建立软链接, 不加-s是硬链接
stat      filename   显示文件详情
du        -hs dir    目录大小
df        -h         各分区使用情况

scp       whiker@localhost:~/data /tmp
nohup     ./start.sh >/dev/null 2>&1 &
env       环境变量
echo      '\n'输出\n字符串和换行符(echo默认追加), -e '\n'输出两行, -e用于转义字符
tar       -zxvf a.tar.gz -C /tmp  其中-v显示解压出的文件名
grep      [opt] file  -v不包含不匹配, -r递归目录, -i忽略大小写, -n显示行号, --include="*.cc"查找文件的类型是.cc
netstat   -at显示listen外的tcp端口, -lt显示监听端口, -r显示路由表
xargs     -i重定义输入位置, {}是占位符, 例如find . -name "*.csv" | xargs -i cp {} ~/data
top       M 按使用内存排序, P 按使用cpu排序

pkg-config
  安装完某个库后，如安装libevent后，会在安装目录的lib/pkgconfig里有后缀名是.pc的文件
  export PKG_CONFIG_PATH=/Users/whiker/bin/libevent/lib/pkgconfig:$PKG_CONFIG_PATH
  执行命令: pkg-config --libs --cflags libevent
    输出 "-I/Users/whiker/bin/libevent/include -L/Users/whiker/bin/libevent/lib -levent"