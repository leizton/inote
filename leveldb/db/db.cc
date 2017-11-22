class DB
> Open(Options& options, string& dbname, DB** dbptr) static
    /*
      测试代码
      Options opts;  opts.create_if_missing=true
      DB* db = NULL
      DB:Open(opts, "/tmp/ldbtest/db1", &db)
    */
    dbptr = NULL
    DBImpl* impl = new DBImpl(options, dbname)
    impl.mutex_.Lock()
    impl.Recover(out VersionEdit edit, out bool save_manifest)
    if impl.mem_ == NULL
        uint64_t new_log_number = impl.versions_.next_file_number_++
        options.env.NewWritableFile(dbname+new_log_number+"log", out WritableFile* logfile)
        edit.SetLogNumber(new_log_number)
        impl.logfile_ = logfile
        impl.logfile_number_ = new_log_number
        impl.log_ = new log::Writer(logfile)
        impl.mem_ = impl.NewMemTable
    if save_manifest
        edit.SetPrevLogNumber(0)
        edit.SetLogNumber(impl.logfile_number_)
        s = impl.version_.LogAndApply(&edit, &impl.mutex_)
    impl.DeleteObsoleteFiles()
    impl.MaybeScheduleCompaction()
    impl.mutex_.Unlock()
    *dbptr = impl
> Put(WriteOptions opt, Slice key, Slice value)
    WriteBatch batch.Put(key, value)
    Write(opt, batch)


class DBImpl : public DB
> (Options& raw_options, string& dbname)
    options_(SanitizeOptions(dbname, ..., raw_options))
    env_(raw_options.env):Env*
    dbname_(dbname)
    mutex_:Mutex
    db_lock_:FileLock*
    logfile_:WritableFile*, logfile_number_:uint64_t
    log_:log::Writer
    mem_:MemTable*
    imm_:MemTable*, has_imm_:AtomicPointer  // 正在压缩(compact)的memTable
    pending_outputs_:set<uint64_t>
    writers_:deque<DBImpl::Writer*>  // 存放当前多个线程一起push_back的Writer
    table_cache_(dbname_, &options_, options_.max_open_files - kNumNonTableCacheFiles):TableCache
    versions_(dbname_, &options_, table_cache_, &internal_comparator_):VersionSet
> NewMemTable():MemTable*
    return new MemTable(internal_comparator_).Ref()
> Recover(VersionEdit* edit, out bool save_manifest)
    env_.CreateDir(dbname_)
    env_.LockFile(dbname_+"/LOCK", &db_lock_)
    if !env_.FileExists(dbname_+"/CURRENT")
        NewDB()  // 创建新的db
    // VersionSet的恢复
    versions_.Recover(save_manifest)
    // 获取db目录下的logfile
    env_.GetChildren(dbname_, out vector<string> filenames)
    vector<uint64_t> logs
    for fname : filenames
        ParseFileName(fname, out uint64_t number, out FileType type)
        if type == kLogFile && (number == versions_.prev_log_number_ || number >= versions_.log_number_)
            logs.push_back(number)
    // 恢复logfile的内容
    std::sort(logs.begin(), logs.end())
    for i = 0:logs.size()
        RecoverLogFile(logs[i], i==logs.size()-1, save_manifest, edit, out uint64_t max_sequence)
        versions_.MarkFileNumberUsed(logs[i])
        versions_.last_sequence_ <?= max_sequence
> NewDB()
    VersionEdit new_db
    new_db.EncodeTo(out string record)
    env_.NewWritableFile(dbname+"/MANIFEST-000001", out WritableFile* file)
    log::Writer log(file).AddRecord(record)
    SetCurrentFile(env_, dbname_, 1)  // filename.cc
> RecoverLogFile(uint64_t log_num, bool last_log, out bool save_manifest, VersionEdit* edit, uint64_t* max_sequence)
    string log_fname = LogFileName(dbname_, log_num)
    env.NewSequentialFile(log_fname, out SequentialFile* file)
    log::Reader reader(file)
    MemTable* mem
    bool compacted = false
    while reader.ReadRecord(out Slice record, string assist)
        mem NULL ?= NewMemTable()
        //
        WriteBatch batch { rep_(record) }
        batch.InsertInto(mem)
        uint64_t last_seq = batch.Sequence + batch.Count - 1  // batch的最后一个操作的seq
        *max_sequence <_ ?= last_seq
        //
        if mem.ApproximateMemoryUsage > options_.write_buffer_size
            compacted = true
            *save_manifest = true
            WriteLevel0Table(mem, edit, NULL)
            mem.Unref();  mem = NULL
    if last_log && !compacted
        env_.GetFileSize(log_fname, out uint64_t filesize)
        env_.NewAppendableFile(log_fname, &logfile_)
        log_ = new log::Writer(logfile_, filesize)
        logfile_number_ = log_num
        mem_ = mem != NULL ? mem : NewMemTable()
    else if mem != NULL
        // 对于非最后一个log文件, memTable存入ssTable
        *save_manifest = true
        WriteLevel0Table(mem, edit, NULL)
> WriteLevel0Table(MemTable* mem, VersionEdit* edit, Version* base)
    // 把mem写成sst文件
    Iterator* iter = new MemTableIterator(mem.table_)  // MemTable.cc
    mutex_.Unlock()
    FileMetaData fmeta = BuildTable(iter)
    mutex_.Lock()
    //
    if fmeta.file_size > 0
        int level = 0
        if base != NULL
            level = base.PickLevelForMemTableOutput(fmeta.smallest.user_key, fmeta.largest.user_key)
        edit.new_files_.push_back(make_pair(level, fmeta))
> BuildTable(Iterator* iter)
    iter.SeekToFirst()
    FileMetaData fmeta
    fmeta.number = versions_.NewFileNumber
    fmeta.smallest.DecodeFrom(iter.key)
    env_.WriteWritableFile(dbname_+ fmeta.number +".ldb", out WritableFile* file)
    auto builder = new TableBuilder(options_, file)
    for ; iter.Valid; iter.Next()
        fmeta.largest.DecodeFrom(iter.key)
        builder.Add(iter.key, iter.value)
    builder.Finish()
    fmeta.file_size = builder.offset
    file.Close()
    return fmeta
//
> Write(WriteOptions opt, WriteBatch* batch)
    Writer* w = new(batch, opt.sync, &mutex_)
    MutexLock l(&mutex_)
    writers_.push_back(w)
    while w != writers_.front && !w.done
        w.cv.Wait()
    if w.done, return
    //
    MakeRoomForWrite(batch == NULL)
    uint64_t last_seq = versions_.last_sequence_
    Writer* last_w = w
    if batch != NULL
        WriteBatch* updates = BuildBatchGroup(&last_w)
        updates.SetSequence(last_seq + 1)
        last_seq += updates.Count
        // 写入log_, 插入mem_
        mutex_.Unlock()
        log_.AddRecord(Slice(updates.rep_))
        if opt.sync, logfile_.Sync()
        updates.InsertInto(mem_)
        mutex_.Lock()
        //
        if updates == tmp_batch_, tmp_batch_.Clear()
        versions_.last_sequence_ = last_seq
    // 把updates的WriteBatch从writers_中清理出去
    while true
        Writer* ready = writers_.front
        writers_.pop_front()
        if ready != w
            ready.done = true
            ready.cv.Signal()
        if ready == last_w
            break
    if !writers_.empty
        writers_.front.cv.Signal()
> BuildBatchGroup(Writer*& last_w):WriteBatch*
    // 把当前writes_的WriteBatch合并成一个
    auto const first = writers_.front  // first不能是NULL
    size_t size = first.batch.rep_.size
    const size_t max_size = size <= 2^17 ? size + 2^17 : 2^20
    WriteBatch* result = first.batch
    //
    for Writer* w : writers_
        if w.sync && !first.sync
            break
        if w.batch != NULL
            size += w.batch.rep_.size
            if size > max_size
                break
            if result == first.batch
                tmp_batch_.Append(result)
                result = tmp_batch_
            result.Append(w.batch)
        *last_w = w  // last_w可能是NULL
    return result
> MakeRoomForWrite(bool force)
    // 已经持有锁mutex_
    bool allow_delay = !force
    while true
        if allow_delay && versions_.NumLevelFiles(0) >= kL0_SlowdownWritesTrigger
            mutex_.Unlock()
            env_.SleepForMicroseconds(1000)
            allow_delay = false
            mutex_.Lock()
        if !force && mem_.ApproximateMemoryUsage <= options_.write_buffer_size
            return
        if imm_ != NULL || versions_.NumLevelFiles(0) >= kL0_SlowdownWritesTrigger
            bg_cv_.Wait()
        else
            uint64_t new_log_number = versions_.NewFileNumber
            env_.NewWritableFile(dbname_+new_log_number+"log", out WritableFile* logfile)
            delete log_, logfile_
            logfile_ = logfile, logfile_number_ = new_log_number
            log_ = new log::Writer(logfile)
            imm_ = mem_, has_imm_.Release_Store(imm_)
            mem_ = this.NewMemTable()
            force = false
            MaybeScheduleCompaction()  // compact
//
> Get(ReadOptions opt, Slice key, string* value)
    MutexLock l(&mutex_)
    uint64_t snapshot =
        if opt.snapshot != NULL, opt.snapshot.number_
        else versions_.last_sequence_
    MemTable* mem(mem_);  mem.Ref()
    MemTable* imm(imm_);  if imm_ != NULL, imm_.Ref()
    Version* current(versions_.current_);  current.Ref()
    //
    Version::GetStats stats
    bool have_stat_update = false
    mutex_.Unlock()
    LookupKey lkey(key, snapshot)
    if !mem.Get(lkey, value) && !(imm != NULL && imm.Get(lkey, value))
        // 从mem和imm都没有找到
        current.Get(opt, lkey, value, &stats)
        have_stat_update = true
    mutex_.Lock()
    //
    if have_stat_update && current.UpdateStats(stats)
        MaybeScheduleCompaction()
    mem.Unref()
    if imm != NULL, imm.Unref()
    current.Unref()
//
> MaybeScheduleCompaction()
    // 已经加了锁
    if !bg_compaction_scheduled_ && (imm_ != NULL || versions_.NeedsCompaction)
        bg_compaction_scheduled_ = true
        env_.Schedule((DBImpl* db) -> {
            MutexLock l(&db.mutex_)
            db.BackgroundCompaction()
            db.bg_compaction_scheduled_ = false
            db.MaybeScheduleCompaction()
            db.bg_cv_.SignalAll()
        }, this)
> BackgroundCompaction()
    if imm_ != NULL
        CompactMemTable()
        return
    Compaction* c = versions_.PickCompaction()
    if c == NULL, return
    if c.IsTrivialMove
        FileMetaData* f = c.inputs_[0][0]
        c.edit_.DeleteFile(c.level_, f.number)
        c.edit_.AddFile(c.level+1, f)
        versions_.LogAndApply(c.edit_, &mutex_)
    else
        CompactionState* compact = new CompactionState(c)
        DoCompactionWork(compact)
        CleanupCompaction(compact)
        c.ReleaseInputs()
        DeleteObsoleteFiles()
> CompactMemTable()
    // 把memTable变成sstTable
    VersionEdit edit
    Version* base = versions_.current_
    base.Ref()
    WriteLevel0Table(imm_, &edit, base)  // 写sst文件
    base.Unref()
    edit.SetLogNumber(logfile_number_)
    versions_.LogAndApply(&edit, &mutex_)
    //
    imm_.Unref(),  imm_=NULL,  has_imm_.Release_Store(NULL)
    DeleteObsoleteFiles()
> DoCompactionWork(CompactionState* compact)
    // snapshots_是一个双链表, 保存所有快照的sequence
    // 每次创建新的snapshot时, 会把最新的sequenceNumber记录到snapshot里
    uint64_t smallest_snapshot_seq = snapshots_.empty ? versions_.LastSequence : snapshots_.oldest.number_
    mutex_.Unlock()
    bool has_current_user_key = false
    string current_user_key
    uint64_t last_seq_for_key = kMaxSequenceNumber
    Iterator* input = versions_.MakeInputIterator(compact.compaction)  // 涉及merge
    for input.SeekToFirst(); input.Valid; input.Next()
        // 先处理imm_
        if has_imm_.NoBarrier_Load() != NULL
            mutex_.Lock()
            if imm_ != NULL
                CompactMemTable()
                bg_cv_.SignalAll()
            mutex_.Unlock()
        //
        Slice key = input.key
        if compact.compaction.ShouldStopBefore(key) && compact.builder != NULL
            FinishCompactionOutputFile(compact, input)
        ParseInternalKey(key, out ParsedInternalKey ikey)
        //
        if !has_current_user_key || ikey.user_key != current_user_key
            has_current_user_key = true
            current_user_key = ikey.user_key  // 更换成新的user_key
            last_seq_for_key = kMaxSequenceNumber
        if last_seq_for_key <= smallest_snapshot_seq /* 被删掉快照的key */ ||
            (ikey.type == kTypeDeletion && ikey.sequence <= smallest_snapshot_seq &&
                compact.compaction.IsBaseLevelForKey(ikey.user_key) /* 更高层的level里没有这个key */
            continue  // 丢弃这个key
        //
        last_seq_for_key = ikey.sequence
        if compact.builder == NULL
            OpenCompactionOutputFile(compact)  // 打开输出文件
        if compact.builder.num_entries == 0
            compact.current_output.smallest.DecodeFrom(key)
        compact.current_output.largest.DecodeFrom(key)
        compact.builder.Add(key, input.value)  // 写入(key, value)
        if compact.builder.FileSize >= compact.compaction.MaxOutputFileSize
            FinishCompactionOutputFile(compact, input)  // 当文件太大时, 写入文件
    if compact.builder != NULL
        FinishCompactionOutputFile(compact, input)
    mutex_.Lock()
> OpenCompactionOutputFile(CompactionState* compact)
    mutex_.Lock()
    uint64_t file_number = versions_.NewFileNumber
    pending_outputs_.insert(file_number)
    CompactionState::Output out(file_number)
    compact.outputs.push_back(out)
    mutex_.Unlock()
    //
    env_.NewWritableFile(TableFileName(dbname_, file_number), &compact.outfile)
    compact.builder = new TableBuilder(options_, compact.outfile)  // sst builder


class Writer
> (WriteBatch* b, bool sync, Mutex* mu)
    batch(b)
    sync(sync)
    cv(mu) : CondVar  // 条件量
    done(false)


class LookupKey
> (Slice key, uint64_t seq)
    // 参考MemTable.cc的Add()方法
    space_[200]:char
    char* p = key.size + 13 < 200 ? space_ : new char[key.size + 13]
    start_(p):char*
    p = EncodeVarint32(p, key.size + 8)
    kstart_(p)  // real_key内容开始的位置
    memcpy(p, key.data, key.size)  // 复制key到p
    p += key.size
    EncodeFixed64(p, (seq << 8) | kValueTypeForSeek)
    end_(p + 8)  // real_key内容结束位置+8
> memtable_key():Slice = Slice(start_, end_-start_)
> user_key():Slice = Slice(kstart_, end_-kstart_-8)  // real_key