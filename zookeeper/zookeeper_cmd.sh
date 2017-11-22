$ ./bin/zkServer.sh start  # 启动zk服务器，配置文件是./conf/zoo.conf

$ ./bin/zkCli.sh -server 127.0.0.1:2181  # 客户端连接zk服务器

[zk: 127.0.0.1:2181(CONNECTED) 0] ls /  # 查看根路径"/"下的所有子节点