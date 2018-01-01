# ref
- https://www.ibm.com/developerworks/cn/linux/l-cn-zerocopy1/index.html
- https://www.ibm.com/developerworks/cn/linux/l-cn-directio/
- https://www.ibm.com/developerworks/cn/linux/l-async/

# 发送磁盘文件到网络的过程:
1. 判断内核的缓冲区内是否有该文件的数据, 如果没有则从磁盘读到内核的缓冲区
   读取由DMA完成, DMA完成后会通知cpu
2. 操作系统把内核缓冲数据copy到read()系统调用指定的用户程序地址空间的某个地址
3. 操作系统把用户空间的数据copy到与网络堆栈相关的内核缓冲区
4. 内核缓冲区数据经过打包后, 发送到网卡(网络接口卡)上
   至少有4次copy

# 零拷贝
避免cpu把数据从一块存储区拷贝到另一块存储区, 从而提高cpu效率
## 零拷贝的目标
避免内核缓冲区之间、内核和用户程序之间的拷贝
将多种操作合在一起, 减少系统调用和上下文切换

# 内存映射
用户程序共享内核的缓冲区, 省去读写时内核和用户空间之间的拷贝