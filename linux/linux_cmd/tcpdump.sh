$ ifconfig
# 记下ip地址

$ sudo tcpdump -i eth0 -nn 'src 10.86.164.25'
# -i eth0 -- 监听网卡eth0
# -nn 'src ...' -- 监听发送方是10.86.164.25
# -nn 'dst ...' -- 监听接收方

$ sudo tcpdump -i eth0 -nnA 'port 80'
$ sudo tcpdump -i eth0 -nnX 'port 80'
# -nnA 'port 80' -- 监听本地80端口的包
# -A -- 以ASCII码打印包内容
# -X -- 以16进制和ASCII码打印包内容