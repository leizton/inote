class task_t
    stCoRoutine_t* co
    int fd


main(int argc, char* argv[])
    ip, port, task_cnt, deamonize = argv[1], atoi(argv[2]), atoi(argv[3]), argc>4 && argv[4].equal("-d")
    //
    int listen_fd = createTcpSocket(ip, port, true)
    listen(listen_fd, 1024)
    setNonBlock(listen_fd)  // netutil.cc
    //
    for proc_i = 0:4
        pid_t pid = fork()
        if pid < 0, break
        if pid > 0, continue
        work(task_cnt)
        exit(0)
    if !deamonize, wait(NULL)

createTcpSocket(char* ip, ushort port, bool reuse)
    //  socket()和setsockopt()在co_hook_syscall.cc里定义
    int fd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)
    if port != 0
        if reuse
            int nReuseAddr = 1
            setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, &nReuseAddr, sizeof(nReuseAddr))
        sockaddr_in addr
        setAddr(ip, port, addr)  // netutil.cc
        bind(fd, (sockaddr*)&addr, sizeof(addr))
    return fd


// 子进程
work(int task_cnt)
    for task_i = 0:task_cnt
        task_t* task = zmalloc(_)
        task.fd = -1
        co_create(&task.co, NULL, readwrite_routine, task)
        co_resume(task.co)
    //
    stCoRoutine_t* accept_co
    co_create(&accept_co, NULL, accept_routine, 0)
    co_resume(accept_co)
    //
    co_eventloop(co_get_epoll_ct(), 0, 0)

static stack<task_t*> g_readwrite

readwrite_routine(void* arg):void*
    co_enable_hook_sys()
    task_t* co = arg
    char buf[16*1024]
    while 1
        if co.fd == -1
            g_readwrite.push(co)
            co_yield_ct()
            continue
        int fd = co.fd
        co.fd = -1
        //
        while 1
            pollfd pf = { fd=fd, events=POLL_IN|POLL_ERR|POLL_HUP }
            co_poll(co_get_epoll_ct(), &pf, 1, 1000, NULL)  // co_routine_epoll_poll.cc
            int ret = read(fd, buf, sizeof(buf))
            if ret > 0
                ret = write(fd, buf, ret)
            if ret <= 0
                close(fd)
                break
    return 0

accept_routine(void*):void*
    co_enable_hook_sys()
    while 1
        if g_readwrite.empty
            pollfd pf = { fd=-1, events=0 }
            poll(&pf, 1, 1000)
            continue
        sockaddr_in addr = {0}
        socklen_t len = sizeof(addr)
        int fd = co_accept(g_listen_fd, &addr, &len)  // co_hook_syscall.cc
        if fd < 0
            pollfd pf = { fd=g_listen_fd, events=POLL_IN|POLL_ERR|POLL_HUP }
            co_poll(co_get_epoll_ct(), &pf, 1, 1000, NULL)  // co_routine_epoll_poll.cc
            continue
        if g_readwrite.empty
            close(fd)
            continue
        setNonBlock(fd)
        task_t* co = g_readwrite.top
        co.fd = fd
        g_readwrite.pop()
        co_resume(co.co)
    return 0