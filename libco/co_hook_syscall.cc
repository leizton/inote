typedef int (*socket_pfn_t)(int domain, type, protocol)

// dlsym: dynamic library symbol, 获取动态库的函数地址或变量地址
static socket_pfn_t g_sys_socket_func = (socket_pfn_t) dlsym(RTLD_NEXT, "socket")

HOOK_SYS_FUNC(name)
    if g_sys_##name##_func == NULL
        g_sys_##name##_func = (name##_pfn_t) dlsym(RTLD_NEXT, #name)


// ------------------------------------------------------------------------------
static rpchook_t* g_rpchook_socket_fd[102400] = {0}

alloc_by_fd(int fd)
    rpchook_t* p = zmalloc(_)
    p.read_timeout.tv_sec = 1
    p.write_timeout.tv_sec = 1
    g_rpchook_socket_fd[fd] = p
    return p

get_by_fd(int fd)
    return g_rpchook_socket_fd[fd]


// ------------------------------------------------------------------------------
socket(int domain, int type, int protocol)
    if !co_is_enable_sys_hook()
        return g_sys_socket_func(domain, type, protocol)
    int fd = g_sys_socket_func(domain, type, protocol)
    rpchook_t *p = alloc_by_fd(fd)
    p.domain = domain
    fcntl(fd, F_SETFL, g_sys_fcntl_func(fd, F_GETFL, 0))
    return fd

setsockopt(int fd, int level, int opt_name, void* opt_value, socklen_t opt_len)
    if !co_is_enable_sys_hook()
        return g_sys_setsockopt_func(fd, level, opt_name, opt_value, opt_len)
    rpchook_t* p = get_by_fd(fd)
    if p && level == SOL_SOCKET
        timeval* val = opt_value
        if opt_name == SO_RCVTIMEO
            memcpy(&p.read_timeout, val, sizeof(*val))
        else if opt_name == SO_SNDTIMEO
            memcpy(&p.write_timeout, val, sizeof(*val))
    return g_sys_setsockopt_func(fd, level, opt_name, opt_value, opt_len)


// ------------------------------------------------------------------------------
co_accept(int fd, sockaddr* addr, socklen_t* len)
    int client = accept(fd, addr, len)
    if client >= 0
        alloc_by_fd(client)
    return client