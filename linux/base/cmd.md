# reference
http://linuxtools-rst.readthedocs.io/zh_CN/latest/base/03_text_processing.html


# base
cd        -          返回最近一次cd前的目录
tree      -L 2       最多显示2层
which     exe        查找可执行文件位置
whereis   file       查找可执行文件和man-page
ln        -s         建立软链接, 不加-s是硬链接
stat      filename   显示文件详情
du        -hs dir    目录大小
df        -h         各分区使用情况
wc        -l         -l只统计行数, 去掉-l统计字符数, "find . -name '*.c' | xargs wc -l"

scp       whiker@localhost:~/data /tmp
          scp -l 8000 $src $dst  限速1MBps, 单位Kbit/s
nohup     ./start.sh >/dev/null 2>&1 &
env       环境变量
echo      '\n'输出\n字符串和换行符(echo默认追加), -e '\n'输出两行, -e用于转义字符
tar       -zxvf a.tar.gz -C /tmp  其中-v显示解压出的文件名
grep      [opt] file  -v不包含不匹配, -r递归目录, -i忽略大小写, -n显示行号, --include="*.cc"查找文件的类型是.cc
netstat   -at显示listen外的tcp端口, -lt显示监听端口, -r显示路由表
xargs     -i重定义输入位置, {}是占位符, 例如find . -name "*.csv" | xargs -i cp {} ~/data
strace    trace进程的某个系统调用

sed       -n '3,6p' a.txt  ; 显示a.txt的[3,6]行

top
  M 按使用内存排序, P 按使用cpu排序, 1 展开每个cpu核
  TIME+ 分钟:秒.毫秒

kill
  kill pid 即 kill -s 15 pid, 向进程发送SIGTERM信号, 进程收到后会释放资源然后停止.
  kill -9 pid 即 kill -s 9 pid, 强制进程立即停止.
  某次kill -9某进程后发现zk节点未被删除, 导致重启时注册失败, 使用kill会执行@PreDestory注解的方法从而删除zk节点.
  所以最好不要加-9

pkg-config
  安装完某个库后，如安装libevent后，会在安装目录的lib/pkgconfig里有后缀名是.pc的文件
  export PKG_CONFIG_PATH=/Users/whiker/bin/libevent/lib/pkgconfig:$PKG_CONFIG_PATH
  执行命令: pkg-config --libs --cflags libevent
    输出 "-I/Users/whiker/bin/libevent/include -L/Users/whiker/bin/libevent/lib -levent"

lsof
  lsof $file      与文件相关
  lsof -c mysql   与进程相关
  lsof -i         网络连接, '-i tcp'只列出tcp, '-i :3306'指定端口号

update-alternatives
  --install <link> <name> <path> <priority>
  --config   选择版本
  --display  显示可选版本
  --remove

od        -N ${num} -t x1 ${file}  ;显示文件前num个字节的16进制, num可以大于文件大小

ifstat          ;查看网络流量
iostat    5     ;查看磁盘流量, 每5秒一次
ethtool   eth0  ;查看网卡速率 Speed: 10000MB/s

free -g
```go
// total = used + free
// 第2行, used-(buffers+cached) free+(buffers+cached)
             total       used       free     shared    buffers     cached
Mem:        129161     127645       1515          0        491      60620
-/+ buffers/cache:      66533      62627
Swap:            0          0          0
```


# cpu num
物理cpu数            `cat /proc/cpuinfo | grep "physical id" | sort | uniq| wc -l`
逻辑cpu数            `cat /proc/cpuinfo | grep "processor" | wc -l`
每个逻辑cpu的core数  `cat /proc/cpuinfo | grep "cpu cores"`
一个物理cpu有多核，即多个逻辑cpu


# detail
## 查看发行版本
cat /proc/version


# 文本
## awk
默认按空白字符分割
> example
awk '{print $2}'
## diff
### git diff
git config --global color.diff auto
git diff a.txt b.txt
### vim diff
dp      把当前窗口的当前行替换到另一个窗口
ctrl+w  切换窗口
ref     https://www.ibm.com/developerworks/cn/linux/l-vimdiff/index.html


# 网络
## lsof
lsof -i tcp:$port
  查看端口被哪个进程占用
## netstat
-a 列出所有
-n 数字形式的ip地址
-t TCP
-p pid
-l LISTEN
> example
netstat -antp | grep LISTEN
netstat -lntp
## ss
一般ss执行比netstat快
> example
ss -lntp


# 进程函数栈
for i in `ps -AL | grep $进程名 | awk '{print $2}'`; do \
  echo === $i ===; \
  gdb --q --n --ex bt --batch --pid $i; \
done 2>&1 |tee /tmp/stacks.txt


# 常用
## grep某个进程的pid
- ps ax | grep 匹配串 | grep -v 'grep' | awk '{print $1}'
- pgrep -fl 匹配串 | awk '{print $1}'
  默认pgrep只会在前15个字符查找，加-f查找全部
## 查看进程命令的绝对路径
ls -l /proc/$pid/cwd


# debug
## gdb
- f 3  调到第3个栈上
- p $varName  打印变量值