# IO多路复用
select()和poll()都是水平触发, 不支持边沿触发

# select()
```c
// excepts包括异常或带外数据
// 成功时select()返回IO就绪的fd数目, 超时返回0, 失败返回-1(错误码在errno)
#include <sys/select.h>
int select(int maxfd_plus_one, fd_set* reads, fd_set* writes, fd_set* excepts, timeval* timeout)
FD_SET(int fd, fd_set* fds)    // 把fd加到fds中
FD_CLR(int fd, fd_set* fds)    // 从fds中移除fd
FD_ISSET(int fd, fd_set* fds)  // 判断fd是否在fds中
FD_ZERO(struct fd_set* fds)    // 清空fds
```
```c
// example
const int fd = STDIN_FILENO;
fd_set reads;
FD_SET(fd, reads);
timeval timeout = { 0, 100000 };  // 100ms
int num = select(fd+1, &reads, NULL, NULL, &timeout);
if (num < 0) {
    printf("select error: %d", errno);
} else if (num > 0) {
    if (FD_ISSET(fd, &reads)) {
        char buf[BUF_LEN + 1];
        int len = read(fd, buf, BUF_LEN);
        if (len < 0) {
            printf("read error: %d", errno);
        }
    }
}
```

# poll()```c
#include <sys/poll.h>
struct pollfd {
    int fd;
    short events;   // 需要监听的事件
    short revents;  // poll返回时触发的事件
};
int poll(pollfd* fds, uint fds_num, int timeout);
```