/etc 存放配置文件
/var 目录存放系统日志
/usr 全称unix software resources

Unix系统组成: 用户空间和内核空间
用户程序不能直接访问内核, 必须通过系统调用

FreeBSD和Linux系统手册页:
  1 一般命令; 2 系统调用; 3 C库函数; 4 特殊文件

<unistd.h> <sys/types.h>
  getpid():pid_t
  getppid():pid_t

万物皆文件，或称资源。如套接字、管道、设备
每个资源在进程中打开时，都有一个"文件描述符(fd)"来标识它

每个进程启动后的３个资源: stdin stdout stderr，它们的fd是0 1 2
一个进程的最大文件描述符软限制一般是1024

进程从父进程处继承环境变量
<stdlib.h>
  setenv(const char* name, const char* value, int overwrite):int
  getenv(const char* name):char*

"argv"是argument vector的缩写

进程退出状态码
<stdlib.h>
  exit(int status)  abort()

<unistd.h>
  fork():pid_t 父进程返回子进程的pid或-1(fail) 子进程返回0
  fork创建的子进程和父进程一模一样, 所以需要复制父进程的内存
  Unix使用CoW(copy-on-write 写时复制)把内存复制延迟到父进程或子进程修改时, 所以之前父子进程还是共享内存

孤儿进程的父进程pid是1

<sys/wait.h>
  wait(int* status):pid_t  // 阻塞至任意一个子进程退出, 并获取退出码
  waitpid(pid_t child, int* status, int option):pid_t  // 等待指定pid的子进程, option可设成阻塞或不阻塞

僵尸进程
  Unix会把进程退出时的状态信息写入队列保存起来, 以便它的父进程通过wait获取
  若某个子进程退出后, 父进程不读取它的退出信息, 则这个子进程变成僵尸进程
  解决方法: 父进程detach子进程; 用孙进程取代子进程(并让孙进程变成孤儿进程)