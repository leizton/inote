# 拉取镜像
docker pull ubuntu:16.04

# 显示所有镜像
docker images  // nginx:v0.1，v0.1是tag

# 启动，停止，删除一个容器
docker run --name webserver -dp 80:80 nginx:v0.1  // 容器名是webserver，镜像是nginx:v0.1
docker ps              // 查看当前正在运行的容器
docker stop webserver
docker rm webserver

# 从容器构建镜像
docker exec -it webserver bash  // -i:交互式，-t:终端方式
root@xxx:/# echo '<h1>v0.2</h1>' > /usr/share/nginx/html/index.html  // 修改欢迎页面
root@xxx:/# exit
docker diff webserver  // 查看具体改动
docker commit -a 'whiker <whiker@163.com>' -m 'updateTo v0.2' webserver whiker/nginx:v0.2  // 提交新定制的镜像
docker images nginx

# push到Docker-Hub
docker login  // 先登录
docker tag nginx:v0.2 whiker/nginx:v0.2  // 修改作者
docker push whiker/nginx:v0.2

# 挂载一个主机目录作为volume
docker run -dit --name network --mount type=bind,source=/Users/whiker/sky/practice,target=/opt/practice ubuntu:16.04 /bin/bash

# 安装vim时出现：Unable to locate package vim
执行apt update, 同步/etc/apt/sources.list和/etc/apt/sources.list.d