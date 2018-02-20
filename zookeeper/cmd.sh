$ ./bin/zkServer.sh start  # 启动zk服务器，配置文件是./conf/zoo.conf

$ ./bin/zkCli.sh -server 127.0.0.1:2181  # 客户端连接zk服务器

[zk: 127.0.0.1:2181(CONNECTED) 0] ls /  # 查看根路径"/"下的所有子节点

create /path 'data'        # 创建持久节点
create -e /ordered ''      # -e临时节点
create -s /ordered/id- ''  # -s有序节点, 创建/zktest/ordered/id-0000000000
                           # 再执行上面的命令创建/zktest/ordered/id-0000000001, -s必须加

get /path  # 第1行显示节点的数据
           # ephemeralOwner=0x161acf0f21c0000, 与该节点绑定的sessionId, 0x0表示持久节点

rmr /path  # 删除节点及其子节点

delete /path [version]  # 可以指定删除哪个版本

set /path 'data' [version]  # 修改数据