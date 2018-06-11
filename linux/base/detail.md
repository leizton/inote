# 配置DNS
/etc/resolv.conf
nameserver 127.0.1.1  // nameserver表明DNS服务器的ip地址

# 查看内核版本
cat /proc/version

# 进程执行文件的完整路径
ls -l /proc/$pid/cwd

# 进程标准输出
vi /proc/$pid/fd/2

# 查看进程关联的socket
lsof -p $pid -nP | grep TCP