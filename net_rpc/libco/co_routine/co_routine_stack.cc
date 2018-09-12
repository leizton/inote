// ------------------------------------------------------------------------------
// stStackMem_t
class stStackMem_t
    stCoRoutine_t* occupy_co
    int stack_size
    char* stack_buffer
    char* stack_bp  // stack_buffer + stack_size

co_alloc_stackmem(uint32_t size)
    stStackMem_t* mem = zmalloc(_)
    mem.occupy_co = NULL
    mem.stack_size = size
    mem.stack_buffer = zmalloc(size)
    mem.stack_bp = mem.stack_buffer + size
    return mem


// ------------------------------------------------------------------------------
// stShareStack_t
class stShareStack_t
    uint alloc_idx
    int stack_size
    int count
    stStackMem_t** stack_array

co_get_stackmem(stShareStack_t* share)
    assert(share != NULL)
    int idx = share.alloc_idx % share.count
    share.alloc_idx++
    return share.share_array[idx]

co_alloc_sharestack(int count, int size)
    stShareStack_t* share = zmalloc(_)
    share.alloc_idx = 0
    share.stack_size = size
    share.count = count
    stStackMem_t** arr = calloc(count, sizeof(stStackMem_t*))
    for i = 0:count
        arr[i] = co_alloc_stackmem(size)
    share.stack_array = arr
    return share