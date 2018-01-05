# ref
- https://www.ibm.com/developerworks/cn/linux/l-cn-directio/
- https://www.ibm.com/developerworks/cn/linux/l-async/

# 标准IO(缓存IO)的优缺点
优点: 如果数据已经在内核缓冲里, 则可以减少读盘次数
缺点: DMA方式不能让数据直接在用户地址空间和磁盘之间传输, 数据拷贝有额外消耗

# 标准IO的同步写和延迟写
同步写, flush()调用, 用户程序阻塞等待数据写入磁盘成功
延迟写(deferred write), write()调用, 数据写到内核的缓冲区就返回
延迟写不能确保数据一定被持久化

# 直接IO
数据直接在用户空间和磁盘之间传输, 不需要页缓存
数据库程序根据存放的数据的特点采用自己的缓存机制而不是操作系统的
直接IO可用于块设备(读写按块进行,可随机存取)或网络设备, 不能用于字符设备(读写按字符进行,实时读写)
## 如何使用
- 打开文件时设置O_DIRECT标识符. sys_open()的执行流程如下:
  1. getname(), 获取用户进程内存空间中的路径名
  2. get_unused_fd(), 从用户进程的文件表中找到一个空闲的文件表指针
     如0-stdin 1-stdout 2-stderr, 然后选空闲的3
  3. do_flip_open(), 执行打开操作
- read()  --> generic_file_read() --> generic_file_aio_read()  --> generic_file_direct_IO()
- write() --> generic_file_read() --> generic_file_aio_write() --> generic_file_direct_IO()
- generic_file_direct_IO()调用block_direct_IO()同步地读写磁盘
- generic_file_aio_read/write()里还会调用file_accessed()修改文件inode的时间戳

# 四种IO
同步阻塞:   app调用read()时, 阻塞等待至内核完成才返回
同步非阻塞: 非阻塞IO上调用read()会立即返回EAGAIN或EWOULDBLOCK, 当内核完成时才返回非负值, 需要app轮询检查
异步阻塞:   使用非阻塞IO, app调用select()阻塞等待可读事件发生
异步非阻塞: 当read()的响应到达时, 会产生一个信号或执行一个基于线程的回调函数, 最终完成这次的IO处理

# AIO: 异步非阻塞
## aiocb
在标准IO中只需要一个唯一句柄来标识IO通道即可使用系统调用完成读写操作
在异步非阻塞中会同时发起多个传输操作, 因此需要为每个传输操作分配一个上下文用于区分和查找
这个上下文的数据结构就是结构体aiocb(aio control block, aio控制块)
  例如 PCB(proc control block)是描述进程上下文的数据结构
```c
struct aiocb {
    int aio_fileds;          // file-descriptor
    volatile void* aio_buf;  // data-buffer
    size_t aio_nbytes;       // data-buffer的字节数
    sigevent aio_sigevent;   // 指明IO操作完成后应进行的操作
    // 其他字段
};
```
## aio_read(aiocb* p):int
aio_error()   检查异步请求的状态, 返回ECANCELLED表示应用程序取消了请求
aio_return()  获取异步请求的返回结果
```c
#include <aio.h>
int fd = open("a.dat", O_RDONLY);
aiocb acb;
bzero(&acb, sizeof(aiocb));
acb.aio_field = fd;
acb.aio_buf = malloc(1024);
acb.aio_nbytes = 1024;
acb.aio_offset = 0;
aio_read(&acb);
while (aio_error(&acb) == EINPROGRESS);
if (aio_return(&acb) > 0) { 从buf获取数据 }
else { 读取操作失败 }
```
## aio_suspend(aiocb* cblst[], int n, timespec* timeout)
挂起当前线程, 直到cblst中某个异步请求完成, 或超时
## 异步回调函数
```c
acb.aio_sigevent.sigev_notify = SIGEV_THREAD;
acb.aio_sigevent.notify_function = aioCompleted;
acb.aio_sigevent.notify_attributes = NULL;
acb.aio_sigevent.sigev_value.sival_ptr = &acb;
aio_read(&acb);
void aioComplete(sigval_t sigval) {
    aiocb* acb = sigval.sival_ptr;
    if (aio_error(acb) != 0) {
        return;  // 未完成或出现其他错误
    }
    // handle
}
```