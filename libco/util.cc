// 获取当前线程id
GetPid():pid_t
    static __thread pid_t pid = 0
    static __thread pid_t tid = 0
    if !pid || !tid || pid != getpid()
        pid = getpid()
        tid = syscall(__NR_gettid)
    return tid

// 获取当前时间
GetTickMS()
    timeval now = {0}
    gettimeofday(&now, NULL)
    return now.tv_sec*1000 + now.tv_usec/1000

// ------------------------------------------------------------------------------
// 把节点p添加到link的尾部
template<class TLink, class TNode>
AddTail(TLink* l, TNode* p)
    if p.link, return  // p已经加入到某个link中了
    if l.tail
        l.tail.next = p
        p.prev = l.tail
        p.next = NULL
        l.tail = p
    else
        p.prev = p.next = NULL
        l.head = l.tail = p
    p.link = l

// 连接l1和l2
template<class TLink, class TNode>
JoinLink(TLink* l1, TNode* l2)
    if l2.head == NULL, return
    for TNode* p = l2.head; p; p = p.next
        p.link = l1
    if l1.tail
        l1.tail.next = l2.head
        l2.head.prev = l1.tail
        l1.tail = l2.tail
    else
        l1.head, l1.tail = l2.head, l2.tail
    l2.head = l2.tail = NULL

template<class TLink, class TNode>
RemoveFromLink(TNode* p)
    if !p.link, return
    if p.prev
        p.prev.next = p.next
    else
        p.link.head = p.next
    if p.next
        p.next.prev = p.prev
    else
        p.link.tail = p.prev
    p.prev = p.next = NULL
    p.link = NULL