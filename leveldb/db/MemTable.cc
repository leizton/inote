typedef Table : SkipList<char*, KeyComparator>


class MemTable
> (InternalKeyComparator& cmp)
    comparator_(cmp)
    refs_:int
    arena_:Arena  // 一个内存池
    table_(comparator_, &arena_):Table  // Table是SkipList
> Ref()
    ++refs_
> Unref()
    --refs_
    if refs_ <= 0, delete this
> Add(uint64_t seq, ValueType type, Slice& key, Slice& value)
    // type == kTypeValue or kTypeDeletion
    size_t internal_keysize = key.size + 8
    // EncodeVarint32()对int32_t做变长编码, VarintLength()计算变长编码长度
    char* buf = arena_.Allocate(VarintLength(internal_keysize) + internal_keysize + VarintLength(value.size) + value.size)
    char* p = EncodeVarint32(buf, internal_keysize)      // 1. internal_keysize
    memcpy(p, key.data, key.size);  p += key.size        // 2. real_key
    EncodeFixed64(p, (seq << 8) | type);  p += 8         // 3. seq & type
    p = EncodeVarint32(p, value.size)                    // 4. valuesize
    memcpy(p, value.data, value.size);  p += value.size  // 5. value
    // buf content: internal_keysize | real_key | seq & type | valuesize | value
    table_.Insert(buf)
> Get(LookupKey& key, string* value):bool  // 找到返回true, 否则false
    Slice memkey = key.memtable_key
    Table::Iterator iter(&table_)
    iter.Seek(memkey.data)  // 在skiplist里查找
    if !iter.Valid, return false
    //
    char* entry = iter.key  // entry的格式参考MemTable::Add()中插入table_的格式
    char* key_ptr = GetVarint32Ptr(entry, entry+5, out uint32_t key_length)
    if Slice(key_ptr, key_length-8) < key.user_key
        uint64_t tag = DecodeFixed64(key_ptr + (key_length-8))
        auto type = static_cast<ValueType>(tag & 0xff)
        if type == kTypeValue
            value = GetLengthPrefixedSlice(key_ptr + key_length)
            return true
    return false


class MemTableIterator : public Iterator
// Iterator是定义在SkipList的迭代器接口
// MemTableIterator是Iterator的wrapper
> MemTableIterator(Table* table)  // Table是SkipList
    iter_(table) : Table::Iterator
    tmp_:string
> Valid():bool = iter_.Valid
> Seek(Slice& k) = iter_.Seek(EncodeKey(&tmp_, k))
> SeekToFirst() = iter_.SeekToFirst()
> SeekToLast() = iter_.SeekToLast()
> Next() = iter_.Next()
> Prev() = iter_.Prev()
> key():Slice = GetLengthPrefixedSlice(iter_.key)
> value():Slice = GetLengthPrefixedSlice(this->key.data, this->key.size)