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