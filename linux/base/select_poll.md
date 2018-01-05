# IO多路复用
select()和poll()都是水平触发, 不支持边沿触发

# select()
## api
```c
#include <sys/select.h>
// http://man7.org/linux/man-pages/man2/select.2.html
// excepts包括异常或带外数据
// 成功时select()返回IO就绪的fd数目, 超时返回0, 失败返回-1(错误码在errno)
int select(int maxfd_plus_one, fd_set* reads, fd_set* writes, fd_set* excepts, timeval* timeout);
FD_SET(int fd, fd_set* fds)    // 把fd加到fds中
FD_CLR(int fd, fd_set* fds)    // 从fds中移除fd
FD_ISSET(int fd, fd_set* fds)  // 判断fd是否在fds中
FD_ZERO(struct fd_set* fds)    // 清空fds
```
## example
```c
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

# poll()
## api
```c
#include <sys/poll.h>
struct pollfd {
    int fd;
    short events;   // 要监听的事件mask
    short revents;  // poll返回时触发的事件mask
};
// http://man7.org/linux/man-pages/man2/poll.2.html
// 每次调用poll时, 内核会自动清空revents
int poll(pollfd* fds, uint fds_num, int timeout_millis);
```
## revents
- POLLIN                            读不会阻塞
- POLLRDNORM, POLLRDBAND, POLLPRI   普通数据/带外数据/紧急数据可读
- POLLIN|POLLPRI                    等同于select()的读事件
- POLLOUT                           写不会阻塞, 等同于select()的写事件
- POLLWRNORM, POLLWRBAND
## example
```c
pollfd fds[2];
fds[0].fd = STDIN_FILENO,  fds[0].events = POLLIN;
fds[1].fd = STDOUT_FILENO, fds[1].events = POLLOUT;
char buf[] = new char[BUF_SIZE];
int r_idx = 0, w_idx = 0;
for (;;) {
    int ret = poll(fds, 2, 1000);
    if (ret < 0) {
        printf("poll error: %d", errno);
        break;
    } else if (ret == 0) {
        continue;
    }
    if (fds[0].revents & POLLIN) {
    }
    if (fds[1].revents & POLLOUT) {
    }
}
```