** epoll资源描述符
// EpollSize: 指定内核监听数目
int epfd = epoll_create(EpollSize);

** epoll_data_t和epoll_event
union epoll_data_t {
    void* ptr;
    int fd;
    uint32_t u32;
    uint64_t u64;
}

struct epoll_event {
    uint32_t events;    // 监听哪些事件, EPOLLET(边缘触发), EPOLLlLT(水平触发)
    epoll_data_t data;  // 用户绑定的数据
}

epoll_event newEvent;
newEvent.events = EPOLLIN | EPOLLOUT;
newEvent.data = socketFd;

** 向epfd注册、修改、删除事件
// op:       EPOLL_CTL_ADD/MOD/DEL, 指定本次对epfd操作的类型
// socketFd: 被监听的socket描述符
int ret = epoll_ctl(epfd, op, socketFd, &newEvent);

** 等待epoll
epoll_event events[EpollSize];
// events:    用于接收内核返回的事件集合
// EpollSize: events数组的大小
// waitTime:  -1,永久阻塞; 0,立即返回; >0,等待的微秒数
// eventNum:  >=0,内核返回的事件数; -1,发生错误,errno查看错误
int eventNum = epoll_wait(epfd, events, EpollSize, waitTime);

** epoll编程框架
int listenFd = socket(AF_INET, SOCKET_STREAM, 0);  // 创建套接字
bind(listenFd, (sockaddr*) &serverAddr, sizeof(serverAddr));
listen(listenFd, 10)
int epfd = epoll_create(EpollSize);
epoll_ctl(epfd, EPOLL_CTL_ADD, listenFd, &listenEvent);
while (true) {
    int eventNum = epoll_wait(epfd, events, EpollSize, 300000);
    if (eventNum < 0) {
        // 打印错误信息
        continue;
    }
    for (int i = 0; i < eventNum; ++i) {
        if (events[i].data.fd == listenFd) {
            // accept
            int clientFd = accept(listenFd, (sockaddr*) &clientAddr, &clientAddrLen);
        }
        else if (event[i].events & EPOLLIN) {
            // read
        }
        else if (event[i].events & EPOLLOUT) {
            // write
        }
    }
}

** 触发模式
1. 水平触发
2. 边缘触发
内核的事件会拷贝到用户空间