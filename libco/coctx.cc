class coctx_t
    void* regs[14]  // x86_64
    size_t ss_size
    char* ss_sp

// 初始化ctx.regs
coctx_make(coctx_t* ctx, coctx_pfn_t pfn, void* s, void* s1)
    char* sp = ctx.ss_sp + ctx.ss_size
    sp = (char*) ((uint64_t) sp & -16LL)  // 低4位置零
    memset(ctx.regs, 0, sizeof(ctx.regs))
    ctx.regs[kRSP] = sp-8
    ctx.regs[kRETAddr] = pfn  // 返回地址
    ctx.regs[kRDI] = s   // 第1个参数
    ctx.regs[kRSI] = s1  // 第2个参数


//-------------
// 64 bit
//low | regs[0]: r15 |
//    | regs[1]: r14 |
//    | regs[2]: r13 |
//    | regs[3]: r12 |
//    | regs[4]: r9  |
//    | regs[5]: r8  | 
//    | regs[6]: rbp |
//    | regs[7]: rdi |
//    | regs[8]: rsi |
//    | regs[9]: ret |  //ret func addr
//    | regs[10]: rdx |
//    | regs[11]: rcx | 
//    | regs[12]: rbx |
//hig | regs[13]: rsp |
enum
	kRDI = 7
	kRSI = 8
	kRETAddr = 9
	kRSP = 13