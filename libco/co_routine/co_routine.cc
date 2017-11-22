// 获取当前线程正在调用的coroutine
GetCurrThreadCo():stCoRoutine_t*
    stCoRoutineEnv_t* env = co_get_curr_thread_env()
    if env, return NULL
    return GetCurrCo(env)

// 获取env的当前coroutine
GetCurrCo(stCoRoutineEnv_t* env):stCoRoutine_t*
    return env.callStack[env.callStackSize - 1]


// ------------------------------------------------------------------------------
// stCoRoutineEnv_t
class stCoRoutineEnv_t
    stCoRoutine_t* callStack[128]
    int callStackSize
    stCoEpoll_t* epoll
    stCoRoutine_t *pending_co, *occupy_co

static stCoRoutineEnv_t* g_arrCoEnvPerThread[204800] = {0}

co_get_curr_thread_env()
    return g_arrCoEnvPerThread[GetPid()]  // util.cc

// 初始化g_arrCoEnvPerThread[GetPid()]
co_init_curr_thread_env()
    auto env = g_arrCoEnvPerThread[GetPid()] = zmalloc(_)
    env.callStackSize = 0
    //
    stCoRoutine_t* self = co_create_env(env, NULL, NULL, NULL)
    self.isMain = 1
    memset(&self.ctx, 0, sizeof(*self.ctx))
    env.pCallStack[env.callStackSize++] = self
    env.epoll = AllocEpoll()  // co_routine_epoll_poll.cc


// ------------------------------------------------------------------------------
// stCoRoutine_t
class stCoRoutine_t
    stCoRoutineEnv_t* env
    pfn_co_routine_t pfn
    void* arg
    coctx_t ctx
    //
    char start=0, end=0, isMain=0, enableSysHook=0, isShareStack
    void* pvEnv=NULL
    stStackMem_t* stack_mem
    //
    uint save_size=0
    char* stack_sp=NULL, save_buffer=NULL
    stCoSpec_t spec[1024]

typedef void* (*pfn_co_routine_t)(void*)

extern "C"
    extern void coctx_swap(coctx_t*, coctx_t*) asm("coctx_swap")

co_create(stCoRoutine_t** co, stCoRoutineAttr_t* attr, pfn_co_routine_t pfn, void* arg)
    if !co_get_curr_thread_env()
        co_init_curr_thread_env()
    *co = co_create_env(co_get_curr_thread_env(), attr, pfn, arg)

co_resume(stCoRoutine_t* co)
    if co.start == 0
        co.start = 1
        coctx_make(&co.ctx, CoRoutineFunc, co, 0)
    //
    auto curr_co = co.env.callStack[env.callStackSize-1]
    co.env.callStack[env.callStackSize++] = co
    co_swap(curr_co, co)

CoRoutineFunc(stCoRoutine_t* co, void*)
    if co.pfn
        co.pfn(co.arg)
    co.end = 1
    co_yield_env(co.env)

// curr_coroutine出栈让出cpu
co_yield_env(stCoRoutineEnv_t* env)
    auto curr = env.callStack[--env.callStackSize]
    auto last = env.callStack[env.callStackSize-1]
    co_swap(curr, last)

co_yield_ct() = co_yield_env(co_get_curr_thread_env())
co_yield(stCoRoutine_t* co) = co_yield_env(co.env)

co_swap(stCoRoutine_t* curr_co, stCoRoutine_t* pending_co)
    auto env = co_get_curr_thread_env()
    if pending_co.isShareStack
        auto occupy_co = pending_co.stack_mem.occupy_co
        pending_co.stack_mem.occupy_co = pending_co
        env.pending_co, env.occupy_co = pending_co, occupy_co
        if occupy_co && occupy_co != pending_co
            save_stack_buffer(occupy_co)
    else
        env.pending_co = env.occupy_co = NULL
    // swap context
    coctx_swap(&curr_co.ctx, &pending_co.ctx)
    //
    auto curr_env = co_get_curr_thread_env()  // stack buffer may be overwrite, so get again
    auto update_pending_co, update_occupy_co = curr_env.pending_co, curr_env.occupy_co
    if update_pending_co && update_occupy_co && update_pending_co != update_occupy_co
        // resume save_buffer
        if update_pending_co.save_buffer && update_pending_co.save_size > 0
            memcpy(update_pending_co.stack_sp, update_pending_co.save_buffer, update_pending_co.save_size)

//
co_free(stCoRoutine_t* co)
    if !co.isShareStack
        free(co.stack_mem.stack_buffer)
        free(co.stack_mem)
    free(co)
co_release(stCoRoutine_t* co) = co_free(co)

//
co_create_env(stCoRoutineEnv_t* env, stCoRoutineAttr_t* attr_param, pfn_co_routine_t pfn, void* arg)
    stCoRoutineAttr_t attr
    if attr_param, memcpy(&attr, attr_param, sizeof(attr))
    attr.stack_size <0 ?= 128*1024
    attr.stack_size >_ ?= 8*1024*1024
    if attr.stack_size & 0xFFF
        attr.stack_size &= ~0xFFF  // 低24位清零
        attr.stack_size += 0x1000
    //
    stStackMem_t* stack_mem
    bool isShareStack = attr.share_stack != NULL
    if isShareStack
        stack_mem = co_get_stackmem(attr.share_stack)
        attr.stack_size = attr.share_stack.stack_size
    else
        stack_mem = co_alloc_stackmem(attr.stack_size)
    //
    stCoRoutine_t* co = zmalloc(_)
    co.env, co.pfn, co.arg = env, pfn, arg
    co.isShareStack = isShareStack
    co.stack_mem = stack_mem
    co.ctx.ss_sp = stack_mem.stack_buffer
    co.ctx.ss_size = attr.stack_size
    return co
