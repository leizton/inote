# timeval
```js
timeval tv1, tv2, tv3;
tv1.tv_sec = 10, tv1.tv_usec = 150000;  // 10.15s

evutil_timeclear(&tv2);  // tv2.tv_sec = tv2.tv_usec = 0

evutil_timeadd(&tv1, &tv2, &tv3);  // tv3 = tv1 + tv2

evutil_timecmp(&tv1, &tv3, ==);    // true, 相等比较
evutil_timecmp(&tv1, &tv2, >);     // true, 大于比较
```

# socket
```js
int evutil_closesocket(evutil_socket_t);

int evutil_make_socket_nonblocking(evutil_socket_t);

// reused bind listen
int evutil_make_listen_socket_reuseable(evutil_socket_t);

// fcntl(fd, FD_SETFD, FD_CLOEXEC)
// FD_CLOEXEC: close on exec, not on fork
// 以exec开头的系统调用函数族, fork子进程执行某个可执行文件
int evutil_make_socket_closeonexec(evutil_socket_t);
```

# errno
```js
#define EVUTIL_SOCKET_ERROR() errno
#define evutil_socket_geterror(sock) errno
#define evutil_socket_error_to_string(errcode) (strerror(errcode))
```

# dns
```js
struct evutil_addrinfo {
    int ai_flags;  // addrinfo简称ai
    int ai_family;
    int ai_socktype;
    int ai_protocol;
    size_t ai_addrlen;
    char* ai_canonname;  // 标准名称
    sockaddr* ai_addr;
    evutil_addrinfo* ai_next;  // 下一个相关的地址信息
};

// @ref http://man7.org/linux/man-pages/man3/getaddrinfo.3.html
// hints    查找条件(查找时的限制条件), 设置ai_family/ai_socktype/ai_protocol/ai_flags这4个字段, 其他字段设0或NULL
// @res     result的简称, 返回的addrinfo可能不止一个
// @return  0,success
int evutil_getaddrinfo(char* node, char* service, evutil_addrinfo* hints, evutil_addrinfo** res);

// example
const char* hostname = "localhost";
const char* port = "8080";

evutil_addrinfo hints;
memset(&hints, 0, sizeof(hints));
hints.ai_family = AF_UNSPEC;  // ipv4 or ipv6
hints.ai_socktype = SOCK_STREAM;
hints.ai_protocol = IPPORT_TCP;
hints.ai_flags = EVUTIL_AI_ADDRCONFIG;
evutil_addrinfo* result;

int err = evutil_getaddrinfo(hostname, port, hints, &result);
if (err) {
    LOG("resolve error: %s", evutil_gai_strerror(err));
    return -1;
}
if (result == nullptr) {
    return -1;
}

evutil_socket_t fd = socket(result->ai_family, result->ai_socktype, result->ai_protocol);
if (fd < 0) {
    return -1;
}
if (connect(fd, result->ai_addr, result->ai_addrlen)) {
    EVUTIL_CLOSESOCKET(fd);
    return -1;
}
```