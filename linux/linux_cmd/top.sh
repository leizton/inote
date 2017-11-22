# 命令
$ top

# 显示
top - 11:38:11 up  1:29,  3 users,  load average: 0.32, 0.38, 0.40
Tasks: 240 total,   1 running, 238 sleeping,   0 stopped,   1 zombie
%Cpu(s): 11.2 us,  4.7 sy,  0.1 ni, 83.9 id,  0.1 wa,  0.1 hi,  0.0 si,  0.0 st
KiB Mem:   8080436 total,  6130744 used,  1949692 free,   240372 buffers
KiB Swap:  8290300 total,        0 used,  8290300 free.  2337496 cached Mem

# 每行含义
第1行: 11:38:11(当前时间) up  1:29(系统运行时间),  3 users(登录用户数),
       load average: 0.32, 0.38, 0.40(最近 1/5/15 分钟内任务队列的平均长度)
第2行: 240 total(进程总数),  1 running(正在运行的进程数),  238 sleeping(睡眠的进程数),
       0 stopped(停止的进程数),  1 zombie(僵尸进程数)

# 选项
P           按cpu排序
M           按内存使用排序
Shift < >   切换排序方式