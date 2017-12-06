class VersionSet
> (string& dbname, Options* options, TableCache* table_cache, InternalKeyComparator* cmp)
    env_(options.env):Env*
    dbname_(dbname)
    options_(options)
    table_cache_(table_cache)
    icmp_(*cmp)
    log_number_(0), prev_log_number_(0)
    last_sequence_(0)
    manifest_file_number_(0), next_file_number_(2)
    descriptor_file_(NULL), descriptor_log_(NULL)
    dummy_versions_(this):Version  // versions循环双链表的head
    current_(NULL):Version*        // 等于dummy_versions_.prev_
    // 初始化current_, 并插入链表
    this.AppendVersion(new Version(this))
> AppendVersion(Version* v)
    if current_ != NULL, current_.Unref()
    current_ = v
    v.Ref()
    // 插入循环双链表的尾部, 即head(dummy_versions_)前面
    v.prev_ = dummy_versions_.prev_
    v.next_ = &dummy_versions_
    v.prev_.next_ = v
    v.next_.prev_ = v
> Finalize(Version* v)
    // 遍历每层, 找出v当前最需要compact的层
    int best_level = 0
    double best_score = v.files_[0].size / kL0_CompactionTrigger  // kL0_CompactionTrigger=4
    for level_i = 1:(kNumLevels-1)
        double score = Version::TotalFileSize(v.files_[level_i]) / (10^level_i MB)  // 10MB 100MB 1000MB
        if score > best_score
            best_level, best_score = level_i, score
    v.compaction_level_, v.compaction_score_ = best_level, best_score
> Recover(bool* save_manifest)
    // 获取当前manifest的文件名
    ReadFileToString(env_, CurrentFileName(dbname_), out string current_desc_filename)  // env.cc
    // 读取当前manifest文件保存的Record
    env_.NewSequentialFile(dbname_+"/"+current_desc_filename, out SequentialFile* file)
    log::Reader reader(file)  // Reader.cc
    VersionSet::Builder builder(this, current_)
    while reader.ReadRecord(out Slice record, string assist)
        VersionEdit edit.DecodeFrom(record)
        builder.Apply(&edit)
        if edit.has_log_number_,        log_number_ = edit.log_number_
        if edit.has_prev_log_number_,   prev_log_number_ = edit.prev_log_number_
        if edit.has_last_sequence_,     last_sequence_ = edit.last_sequence_
        if edit.has_next_file_number_,  next_file_number_ = (manifest_file_number_ = edit.next_file_number_) + 1
    // 恢复
    Version* v = new Version(this)
    builder.SaveTo(v)
    Finalize(v)
    AppendVersion(v)
    *save_manifest = !ReuseManifest(current_desc_filename)
> ReuseManifest(string dscname)
    env_.GetFileSize(dscname, out uint64_t manifest_size)
    if manifest_size > options.max_file_size
        return false
    env_.NewAppendableFile(dbname_+"/"+dscname, out descriptor_file_)
    descriptor_log_ = new log::Writer(descriptor_file_, manifest_size)
    ParseFileName(dscname, out uint64_t manifest_num, ...)  // filename.cc
    manifest_file_number_ = manifest_num
    return true
> NewFileNumber():uint64_t = next_file_number_++
> NumLevelFiles(int level):int
    // 返回第level层的文件个数
    return current_.files_[levle].size
> NeedsCompaction()
    return current_.compaction_score_ >= 1 || current_.file_to_compact_ != NULL
//
> LogAndApply(VersionEdit* edit, Mutex* mu)
    if !edit.has_log_number_
        edit.SetLogNumber(log_number_)
    if !edit.has_prev_log_number_
        edit.SetPrevLogNumber(prev_log_number_)
    edit.SetNextFile(next_file_number_)
    edit.SetLastSequence(last_sequence_)
    // 新的Version
    Version* v = new Version(this)
    VersionSet::Builder builder(this, current_)
    builder.Apply(edit)
    builder.SaveTo(v)
    Finalize(v)
    // 获取日志
    string new_manifest_file
    if descriptor_log_ == NULL
        new_manifest_file = DescriptorFileName(dbname_, manifest_file_number_)
        env_.NewWritableFile(new_manifest_file, &descriptor_file_)
        descriptor_log_ = new log::Writer(descriptor_file_)
        WriteSnapshot(descriptor_log_)  // 写snapshot
    // edit记录到log中
    mu.Unlock()
    edit.EncodeTo(out string record)
    descriptor_log_.AddRecord(record)
    descriptor_file_.Sync()
    if !new_manifest_file.empty
        SetCurrentFile(env_, dbname_, manifest_file_number_)  // filename.cc
    mu.Lock()
    // 添加新创建的version
    AppendVersion(v)
    log_number_ = edit.log_number_
    prev_log_number_ = edit.prev_log_number_
//
> PickCompaction():Compaction*
    Compaction* c
    int level = 0
    if current_.compaction_score_ >= 1  // size_compaction
        level = current_.compaction_level_  // compaction_level_在VersionSet::Finalize()里更新
        c = new Compaction(options_, level)
        // 找到在compact_pointer_[level]的后一个文件
        for FileMetaData* f : current_.files_[level]
            if compact_pointer_[level].empty || f.largest > compact_pointer_[level]
                c.inputs_[0].push_back(f)
                break
        if c.inputs_[0].empty
            c.inputs_[0].push_back(current_.files_[level][0])
    else if current_.file_to_compact_ != NULL  // seek_compaction
        level = current_.file_to_compact_level_
        c = new Compaction(options_, level)
        c.inputs_[0].push_back(current_.file_to_compact_)
    else
        return NULL
    c.input_version_ = current_
    c.input_version_.Ref()
    if level == 0
        // GetRange()找出c.inputs_[0]的所有sst文件的最小和最大key
        GetRange(c.inputs_[0], out InternalKey smallest, out InternalKey largest)
        // 把第level层上有[smallest, largest]之间key的sst文件都放入c.inputs_[0], 扩大compact
        current_.GetOverlappingInputs(0, smallest, largest, &c.inputs_[0])
    this.SetupOtherInputs(c)  // 扩展c.inputs_[1]
    return c
//
> MakeInputIterator(Compaction* c)
    const int space = c.level_ == 0 ? c.inputs_[0].size+1 : 2
    Iterator** list = new Iterator*[space]
    int num = 0
    for which = 0:2
        if c.inputs_[which].empty, continue
        if c.level == 0 && which == 0
            for FileMetaData* f : c.inputs_[which]
                list[num++] = table_cache_.NewIterator(opt, f.number, f.file_size)
        else
            // TwoLevelIterator通过GetFileIterator()获取table_cache_的迭代器
            list[num++] = new TwoLevelIterator(
                new Version::LevelFileNumIterator(&c.inputs_[which]),
                &VersionSet::GetFileIterator, table_cache_, opt)
    return Merger::NewMergingIterator(list, num)


class Builder
> 内部类
    // FileMetaData定义在VersionEdit.cc
    struct BySmallestKey  // FileMetaData的比较器
        InternalKeyComparator* internal_comparator
        operator()(FileMetaData* f1, f2):bool
            // 先比较最小的key, 再比较number
            int r = internal_comparator.Compare(f1.smallest, f2.smallest)
            return r!=0 ? r<0 : f1.number<f2.number
    struct LevelState
        deleted_files :set<uint64_t>
        added_files   :set<FileMetaData*, BySmallestKey>*  // 指定比较器的set
> (VersionSet* vset, Version* base)
    vset_(vset)
    base_(base)  // builder前已有的Version
    levels_[kNumLevels]:LevelState
    base_.Ref()
    for level_i = 0:kNumLevels
        levels[level_i].added_files = new set<FileMetaData*, BySmallestKey>(BySmallestKey cmp)
> Apply(VersionEdit* edit)
    //
    for pair<int, InternalKey> cp : edit.compact_pointers_
        vset_.compact_pointer_[cp.first] = cp.second.Encode().ToString()
    // 已删除的文件的number
    for pair<int, uint64_t> dp : edit.deleted_files_
        levels_[dp.first].deleted_files.insert(dp.second)
    //
    for pair<int, FileMetaData> np : edit.new_files_
        FileMetaData* f = new FileMetaData(np.second)
        f.refs = 1
        f.allowed_seeks = Math.max(f.file_size / 2^14, 100)
        levels_[np.first].deleted_files.erase(f.number)
        levels_[np.first].added_files.insert(f)
> SaveTo(Version* version)
    for level_i = 0:kNumLevels
        vector<FileMetaData*>& base_files = base_.files_[level_i]
        set<FileMetaData*>& added_files = levels_[level_i].added_files
        version.files_[level_i].reserve(base_files.size + added_files.size)
        // 合并base_files和added_files
        int baseIdx = 0
        for FileMetaData* f : added_files
            while baseIdx < base_files.size && base_files[baseIdx] < f  // 用BySmallestKey比较两个FileMetaData
                MaybeAddFile(version, level_i, base_files[baseIdx])
                ++baseIdx
            MaybeAddFile(version, level_i, f)
        while baseIdx < base_files.size
            MaybeAddFile(version, level_i, base_files[baseIdx++])
> MaybeAddFile(Version* version, int level_i, FileMetaData* f)
    if levles_[level_i].deleted_files.count(f.number) <= 0  // 没有被删除
        ++f.refs
        version.files_[level_i].push_back(f)