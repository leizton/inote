1. 安装完某个库后，如安装libevent后，会在安装目录的lib/pkgconfig里有后缀名是.pc的文件
2. export PKG_CONFIG_PATH=/Users/whiker/bin/libevent/lib/pkgconfig:$PKG_CONFIG_PATH
3. 执行命令
   $ pkg-config --libs --cflags libevent
   输出 "-I/Users/whiker/bin/libevent/include -L/Users/whiker/bin/libevent/lib -levent"