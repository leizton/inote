class TableCache
> (string dbname, Options* opt, int cache_capacity)
    env_(opt.env)
    dbname_(dbname)
    options_(opt)
    cache_(new ShardedLRUCache(cache_capacity))
//
> Get(ReadOptions opt, uint64_t file_number, uint64_t file_size, Slice internal_key,
      void* arg, void (*saver)(void*, Slice, Slice))
    // Cache::Handle是struct{}, 一个空结构体，作用类似void*
    this.FindTable(file_number, file_size, out Cache::Handle* handle)
    Table* t = ((TableAndFile*) handle.value).table
    t.InternalGet(opt, internal_key, arg, saver)
    cache_.Release(handle)
//
> FindTable(uint64_t file_number, uint64_t file_size, Handle** handle)
    EncodeFixed64(out char[8] tmp, file_number)
    Slice key(tmp, 8)
    *handle = cache_.Lookup(key)
    if *handle != NULL, return
    //
    string fname = TableFileName(dbname_, file_number)
    RandomAccessFile* file
    if !env_.NewRandomAccessFile(fname, &file).ok
        env_.NewRandomAccessFile(SSTTableFileName(dbname_, file_number), &file)
    //
    Table::Open(*options_, file, file_size, out Table table)
    TableAndFile* tf = new { file, table }
    *handle = cache_.Insert(key, tf, 1, (Slice key, void* value) -> delete tf.file, tf.table)