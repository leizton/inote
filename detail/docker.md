# 拉取镜像
docker pull ubuntu:16.04

# 显示所有镜像
docker images           // nginx:v0.1，v0.1是tag
docker rmi ${image_id}  // 删除某个镜像

# 启动，停止，删除一个容器
docker run --name webserver -dp 80:80 nginx:v0.1  // 容器名是webserver，镜像是nginx:v0.1
docker ps -a           // 查看所有容器, STATUS表示启动到现在经过的时间, 或者是Exited(已经stop了)
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
docker run -dit --name network --mount type=bind,source=/Users/whiker/sky/practice,target=/opt/practice whiker/ubuntu16:dev /bin/bash

# 安装vim时出现：Unable to locate package vim
执行apt update, 同步/etc/apt/sources.list和/etc/apt/sources.list.d

# example
docker pull ubuntu:18.04
docker run -dit --rm --name $container_name ubuntu:18.04 /bin/bash
  -d detach, 分离模式, 后台运行, 不随docker命令结束而结束;
  -i 保持stdin打开;  -t 分配伪终端;  --rm stop容器时删除容器
docker ps -a
docker exec -it $container_name /bin/bash
  apt update
  apt install gcc g++ make openssl curl wget vim
  install python
    wget https://www.python.org/ftp/python/3.7.0/Python-3.7.0.tg
    tar -zxf Python-3.7.0 && cd Python-3.7.0
    ./configure && make && make install
    whereis python3
    ln -s /usr/local/bin/python3 /usr/bin/python
  adduser dev, su dev
docker commit -a '$author_name $author_email' -m 'comment' $container_name $new_image_name:$new_image_tag
docker login  &&  docker tag $image_name:$tag $user_name/$image_name:$tag  &&  docker push $user_name/$image_name:$tag
docker attach $container_name