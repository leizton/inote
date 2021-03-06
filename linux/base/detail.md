# 配置DNS
/etc/resolv.conf
nameserver 127.0.1.1  // nameserver表明DNS服务器的ip地址

# 进程执行文件的完整路径
ls -l /proc/$pid/cwd

# 进程标准输出
vi /proc/$pid/fd/2

# 查看进程关联的socket
lsof -p $pid -nP | grep TCP

# crontab
```go
crontab -l     // 列出
crontab -r     // 删除全部
crontab $file  // 启动
  // 文件内容，文件末尾必须有一个空白行
  // run.sh run.log 必须写全路径
  */1 * * * * /tmp/run.sh > /tmp/run.log 2>&1
/var/log/cron  // for debug
```
`crontab -l`显示无结果，但`sudo crontab -l`可能有结果
@ref https://linuxtools-rst.readthedocs.io/zh_CN/latest/tool/crontab.html

# meminfo
cat /proc/meminfo
--
buffers + cached = Active(file) + Inactive(file) + Shmem
Active = Active(anon) + Active(file)
Active(anon): 进程堆上的匿名内存
Active(file): 磁盘高速缓存/文件映射等与磁盘文件相对应的内存空间
Shmem: tmpfs(利用内存提供RAM磁盘)使用的内存

# /proc/$pid/status
VmSize    当前使用的虚拟内存大小
VmPeak    VmSize的峰值
VmRSS     当前使用的物理内存大小
VmHWM     VmRSS的峰值
VmData    数据段大小
VmStk     栈大小
@ref http://hutaow.com/blog/2014/08/28/display-process-memory-in-linux/

# /proc/$pid/stat
utime  用户态cpu时钟数, 多核时会翻倍
stime  内存态cpu时钟数
@ref https://stackoverflow.com/questions/16726779/how-do-i-get-the-total-cpu-usage-of-an-application-from-proc-pid-stat