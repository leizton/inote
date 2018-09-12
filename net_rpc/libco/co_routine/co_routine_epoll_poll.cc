// ------------------------------------------------------------------------------
// stCoEpoll_t
class stCoEpoll_t
    static const int _EPOLL_SIZE = 1024_0
    int epollFd
    stTimeout_t* timeout
    stTimeoutItemLink_t* timeoutList
    stTimeoutItemLink_t* activeList
    co_epoll_res* result

AllocEpoll()
    stCoEpoll_t* ctx = zmalloc(_)
    ctx.epollFd = epoll_create(stCoEpoll_t::_EPOLL_SIZE)  // <sys/epoll.h>
    ctx.timeout = AllocTimeout(size = 60_000)  // co_routine_timeout.cc
    ctx.activeList = zmalloc(_)
    ctx.timeoutList = zmalloc(_)
    return ctx

FreeEpoll(stCoEpoll_t* ctx)
    if ctx
        free(ctx.activeList)
        free(ctx.timeoutList)
        FreeTimeout(ctx.timeout)
        co_epoll_res_free(ctx.result)
        free(ctx)


co_get_epoll_ct():stCoEpoll_t*
    if !co_get_curr_thread_env()
        co_init_curr_thread_env()
    return co_get_curr_thread_env().epoll


// ------------------------------------------------------------------------------
// stPoll_t
class stPoll_t : stTimeoutItem_t
    int epollFd
    pollfd* fds
    nfds_t nfds
    stPollItem_t* pollItems
    int allEventDetach

class stPollItem_t
    pollfd* self
    stPoll_t* poll
    epoll_event event

typedef int (*poll_pfn_t)(pollfd fds[], nfds_t nfds, int timeout)

co_poll(stCoEpoll_t* ctx, pollfd fds[], nfds_t nfds, int timeout, poll_pfn_t pollfn):int
    if timeout == 0
        return pollfn(fds, nfds, timeout)
    timeout <0 ?= INT_MAX
    bool isShareStack = GetCurrThreadCo().isShareStack
    //
    stPollItem_t arr[2]
    stPoll_t* pollEntry = zmalloc(_)
    pollEntry.epollFd = ctx.epollFd
    pollEntry.fds = calloc(nfds, _)
    pollEntry.nfds = nfds
    pollEntry.pollItems = (nfds<2 && !isShareStack) ? arr : calloc(nfds, sizeof(stPollItem_t))
    pollEntry.pfnProcess = (stTimeoutItem_t* p) -> co_resume(p.arg)
    pollEntry.arg = GetCurrThreadCo()
    //
    for i = 0:nfds
        stPollItem_t* pollItem = pollEntry.pollItems + i
        pollItem.self = pollEntry.fds+i
        pollItem.poll = &pollEntry
        pollItem.pfnPrepare = OnPollPreparePfn
        epoll_event& ev = pollItem.event
        if fds[i].fd > -1
            ev.data.ptr = pollItem
            ev.events = PollEvent2Epoll(fds[i].events)
            co_epoll_ctl(epfd, EPOLL_CTL_ADD, fds[i].fd, &ev)
    //
    uint64_t now = GetTickMS()
    pollEntry.expireTime = now + timeout
    AddTimeout(ctx.timeout, &pollEntry, now)
    co_yield_env(co_get_curr_thread_env())
    //
    RemoveFromLink(&pollEntry)
    for i = 0:nfds
        if fds[i].fd > -1
            co_epoll_ctl(epfd, EPOLL_CTL_DEL, fds[i].fd, &pollEntry.pollItems[i].event)
        fds[i].revents = pollEntry.fds[i].revents
    free(pollEntry.pollItems != arr ? pollEntry.pollItems : NULL)
    free(pollEntry.fds)
    free(&pollEntry)