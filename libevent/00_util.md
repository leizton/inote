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