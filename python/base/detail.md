# 获取本机ip
> 通过udp
`python -c "import socket;s=socket.socket(socket.AF_INET, socket.SOCK_DGRAM);s.connect(('8.8.8.8', 53));print(s.getsockname()[0]);s.close()"`