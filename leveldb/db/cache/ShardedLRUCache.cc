typedef void (*Deleter)(const Slice& key, void* value)

kNumShardBits = 4
kNumShards = 1 << kNumShardBits

class ShardedLRUCache  // 分片lru_cache
> (size_t capacity)
    shard_[kNumShards]:LRUCache((capacity + kNumShards - 1) / kNumShards)
    id_mutex_:Mutex
    last_id_:uint64_t
> Shard(Slice key)
    return &shard_[ key.hash >> (32 - kNumShardBits) ]
> Insert(Slice key, void* value, size_t charge, Deleter deleter)
    // charge: 索要的空间
    return Shard(key).Insert(key, value, charge, deleter)
> Lookup(Slice key)
    return Shard(key).Lookup(key)


class LRUCache
> (size_t capacity)
    capacity_(capacity) const
    table_:HandleTable
    lru_:LRUHandle
    in_use_:LRUHandle
    usage_:size_t
    mutex_:Mutex
> LRU_Append(LRUHandle& list, e)  // 向list尾部插入e
    e.next = list
    e.prev = list.prev
    e.next.prev = e
    e.prev.next = e
> LRU_Remove(LRUHandle* e)  // 把e从双链表中移出
    e.next.prev = e.prev
    e.prev.next = e.next
> LRU_MoveTo(LRUHandle* e, LRUHandle& list)  // 把e从原先的链表移到list
    LRU_Remove(e)
    LRU_Append(list, e)
> Unref(LRUHandle* e)
    e.refs--
    if e.refs == 0  // e不在缓存中
        e.deleter(e.key, e.value)
        free(e)
    else  // e在缓存中
        LRU_MoveTo(e, lru_)  // 从in_use_移到lru_
> FinishErase(LRUHandle* e):bool  // 把e从缓存中移出
    if e == NULL, return false
    LRU_Remove(e)
    e.in_cache = false
    usage_ -= e.charge
    Unref(e)
> Insert(Slice key, void* value, size_t charge, Deleter deleter):Handle*
    MutexLock l(&mutex_)
    LRUHandle* e = new(zmalloc(sizeof(LRUHandle)-1 + key.size)) LRUHandle(key, value, charge, deleter)
    e.refs++
    e.in_cache = true
    // 插入新的entry
    LRU_Append(in_use_, e)
    usage_ += e.charge
    FinishErase(table_.Insert(e))
    // lru清理
    while usage_ > capacity_ && lru_.next != &lru_
        LRUHandle* old = lru_.next
        FinishErase(table_.Remove(old.key, old.hash))
    return e
> Lookup(Slice key)
    MutexLock l(&mutex_)
    LRUHandle* e = table_.Lookup(key, key.hash)
    if e != NULL
        if e.refs == 1 && e.in_cache
            // 如果e在lru_中, 则从lru_移到in_use_
            // 如果e不在lru_中, 也添加到in_use_的末尾
            LRU_MoveTo(e, in_use_)
        e.refs++
    return e
> Release(Cache::Handle* e)
    // 用于外部释放, 加锁保护e.refs和in_use_、lru_
    MutexLock l(&mutex_)
    Unref(e)  // 如果e在in_use_中, 则会被移到lru_


class LRUHandle
// Map.Entry
> (Slice key, void* value, size_t charge, Deleter deleter)  // LRUHandle对象在LRUCache::Insert()里被new出来
    next_hash, next, prev : LRUHandle*
    in_cache(false):bool
    refs(1):uint32_t  // 初始化1, HandleTable::Insert()的调用者通过LRUCache::Release()释放
    hash(key.hash):uint32_t
    charge(charge)    // 存放的kv对的权重
    value(value)
    deleter(deleter)  // 在LRUHandle被free前，执行该回调，@ref LRUCache::Unref()
    key_length(key.size)
    key_data:char[1]  // 占位数组, 存key
    memcpy(key_data, key.data, key.size)
> key()
    return next == this ? *(Slice*) value : Slice(key_data, key_length)


class HandleTable  // 拉链法
> ()
    buckets_:LRUHandle**
    bucket_num_:uint32_t
    elem_num_:uint32_t
    this.Resize()
> Resize()
    uint32_t new_bucket_num = max(4, 2 ^ int(log2(elem_num_)))
    LRUHandle** new_buckets = new LRUHandle*[new_bucket_num] { NULL }
    for i = 0:bucket_num_
        LRUHandle* e = buckets_[i]
        while e != NULL
            LRUHandle** new_bucket = &new_buckets[e.hash & (new_bucket_num - 1)]
            LRUHandle* old_next_hash = e.next_hash
            e.next_hash = *new_bucket
            *new_bucket = e
            e = old_next_hash  // 由于bucket_num改变了, 所以old_next_hash和e不一定在同一个桶中
    delete[] buckets_
    buckets_ = new_buckets, bucket_num_ = new_bucket_num
> FindPointer(Slice key)
    LRUHandle** ptr = &buckets_[key.hash & (bucket_num_ - 1)]
    // *ptr是一个bucket
    while *ptr != NULL && ( (*ptr).hash != key.hash || (*ptr).key != key )
        ptr = &(*ptr).next_hash  // 如果(*ptr).next_hash==NULL, 则赋值后*ptr==NULL
    return ptr
> Insert(LRUHandle* h)
    LRUHandle** ptr = FindPointer(h.key)
    LRUHandle* old = *ptr
    h.next_hash = old == NULL ? NULL : old.next_hash
    *ptr = h  // 尾部插入(h.key是新的), 或替换(已存在h.key), ptr指向前一个的next_hash或者buckets_[index]
    if old == NULL
        if ++elem_num_ > bucket_num_, this.Resize()
    return old
> Remove(Slice key)
    LRUHandle** ptr = FindPointer(h.key)
    LRUHandle* target = *ptr
    if *ptr != NULL
        // 存在这个key
        *ptr = *ptr.next_hash
        --elem_num_
    return target