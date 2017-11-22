typedef void (*OnPreparePfn_t)(stTimeoutItem_t*, epoll_event &ev, stTimeoutItemLink_t*)
typedef void (*OnProcessPfn_t)(stTimeoutItem_t*)


class stTimeoutItem_t
    enum { eMaxTimeout=40_000 }  // 40 sec
    stTimeoutItem_t* prev, next
    stTimeoutItemLink_t* link
    //
    uint64_t expireTime
    OnPreparePfn_t prepare, process
    void* arg
    bool timeout

class stTimeoutItemLink_t
    stTimeoutItem_t *head, *tail

class stTimeout_t
    stTimeoutItemLink_t* items
    int itemSize
    uint64_t start
    int64_t startIdx


AllocTimeout(int size):stTimeout_t
    stTimeout_t* p = zmalloc(_)
    p.itemSize = size
    p.items = zmalloc(size * sizeof(stTimeoutItemLink_t))
    p.start = GetTickMS()  // util.cc
    p.startIdx = 0
    return p

FreeTimeout(stTimeout_t* timeout)
    free(timeout.items)
    free(timeout)

AddTimeout(stTimeout_t* timeout, stTimeoutItem_t* item, uint64_t now)
    if timeout.start == 0
        timeout.start = now
        timeout.startIdx = 0
    assert item.expireTime >= now >= timeout.start
    //
    uint64_t diff = item.expireTime - timeout.start
    assert diff < timeout.itemSize
    AddTail(timeout.items + (timeout.startIdx+diff)%timeout.itemSize, item)  // util.cc

// 获取到达时间的items
TakeAllTimeout(stTimeout_t* timeout, uint64_t now, stTimeoutItemLink_t* result)
    if timeout.start == 0
        timeout.start = now
        timeout.startIdx = 0
    if now < timeout.start
        return
    int cnt = min(now - timeout.start + 1, timeout.itemSize)
    if cnt < 0, return
    for i = 0:cnt
        int idx = (timeout.start+i) % timeout.itemSize
        JoinLink(result, timeout.items+idx)  // util.cc
    timeout.start = now
    timeout.startIdx += cnt - 1