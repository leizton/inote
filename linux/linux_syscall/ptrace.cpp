#include <sys/ptrace.h>
int ptrace(int request, int pid, int addr, int data);
// 让父进程控制子进程, 用于实现断点调试(GDB)

进程的内存布局
	Code segment   存放二进制代码, 也称Text segment
	Data segment   常量字符串, 全局变量, 静态变量
	Data
	BSS
	Heap           堆区, new/delete, malloc/calloc/free
	Stack segment  函数调用栈区

// request的取值定义在"/usr/include/x86_64-linux-gnu/sys/ptrace.h"
enum __ptrace_request {
	PTRACE_TRACEME = 0,
	
	// 取出子进程的内存数据, addr指定内存地址, data存放结果
	PTRACE_PEEKTEXT = 1,  // return the word(一个cpu字长) in the process's text-space(Text区)
	PTRACE_PEEKDATA = 2,  // the process's data space
	PTRACE_PEEKUSER = 3,  // the process's user area
	
	// 写子进程的内存数据
	PTRACE_POKETEXT = 4,  // 把data写到Text segment
	PTRACE_POKEDATA = 5,
	PTRACE_POKEUSER = 6,
	
	PTRACE_CONT = 7,      // continue the process
	...
	PTRACE_ATTACH = 16,   // attach to a process that is already running
};