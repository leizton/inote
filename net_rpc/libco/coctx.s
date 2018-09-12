.globl coctx_swap
.type coctx_swap, @function
coctx_swap:
    ;rdi(第1个参数)是curr_co.ctx的地址
	;rsi(第2个参数)是pending_co.ctx的地址
	leaq 8(%rsp),%rax    ;把[rsp+8]这个地址值存入rax，这个地址是父函数(curr_co)栈的rsp(栈顶)
	                     ;lea指令用来赋值指针，而不是指针所指的内容
	leaq 112(%rdi),%rsp  ;此时把curr_co.ctx.regs占用内存的最高地址值赋给rsp，
	                     ;从而实现把curr_co.ctx.regs的内存块当作函数栈来使用
    ;下面的一系列pushq把rax rbx等值写到curr_co.ctx.regs里
	pushq %rax      ;regs[13]存curr_co的rsp
	pushq %rbx
	pushq %rcx
	pushq %rdx
	pushq -8(%rax)  ;regs[9]存返回地址，即curr_co的下一条指令的地址
	pushq %rsi
	pushq %rdi
	pushq %rbp
	pushq %r8
	pushq %r9
	pushq %r12
	pushq %r13
	pushq %r14
	pushq %r15
    ;把pending_co.ctx.regs的内存块当作函数栈来使用
	;从pending_co.ctx.regs恢复各个寄存器
	movq %rsi, %rsp
	popq %r15
	popq %r14
	popq %r13
	popq %r12
	popq %r9
	popq %r8
	popq %rbp
	popq %rdi
	popq %rsi
	popq %rax       ;regs[9]，pending_co的恢复地址
	popq %rdx
	popq %rcx
	popq %rbx
	popq %rsp       ;切到了pending_co的函数栈的rsp
	;
	pushq %rax      ;压入pending_co的恢复地址，供下面ret指令使用
	xorl %eax, %eax ;eax清零
	ret             ;弹出pending_co的恢复地址，跳转至恢复地址执行