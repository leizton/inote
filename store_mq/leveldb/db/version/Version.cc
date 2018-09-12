静态
> Version::TotalFileSize(vector<FileMetaData*> files):uint64_t
    uint64_t sum = 0
    for FileMetaData* f : files
        sum += f.file_size
    return sum
> Version::FindFile(vector<FileMetaData*>& files, Slice key)
    // 二分查找
    int low = 0, high = files.size
    while low < high
        int mid = (low + high) / 2
        if key >= files[mid].largest.user_key
            low = mid + 1
        else
            high = mid
    return high
> Version::SomeFileOverlapsRange(bool disjoint_sorted_files, vector<FileMetaData*>& files, Slice min_key, Slice max_key)
    // 对于level=0的ssTable, files的所有文件的key范围有可能有交集, 且files是无序的, 所以disjoint_sorted_files是false
    if !disjoint_sorted_files
        // 文件间有交集, 或无序
        for FileMetaData* f : files
            if !(min_key > f.largest.user_key || max_key < f.smallest.user_key)
                return true  // 有交集
        return false
    int index = FindFile(files, min_key)
    return index < files.size && max_key >= files[index].smallest.user_key


class Version
> (VersionSet* vset)
    vset_(vset):VersionSet*
    next_(this):Version*
    prev_(this):Version*
    refs_(0):int
    files_[kNumLevels]:vector<FileMetaData*>  // 二维数组, 每层当前的所有FileMetaDatas
    file_to_compact_:FileMetaData*
    file_to_compact_level_:int
    compaction_score_(-1):double  // score>=1时, 须做压缩
    compaction_level_:int
> PickLevelForMemTableOutput(Slice min_key, Slice max_key)
    // 选择一个更高的level, 把新的sst文件放到更高的level里
    if OverlapInLevel(0, min_key, max_key), return 0
    InternalKey begin(min_key, kMaxSequenceNumber, kTypeValue)
    InternalKey end(max_key, 0, kTypeDeletion)
    for level = 0:kMaxMemCompactLevel  // kMaxMemCompactLevel == 2
        if OverlapInLevel(level+1, min_key, max_key)
            return level
        if level + 2 < kNumLevels
            GetOverlappingInputs(level + 2, &begin, &end, out vector<FileMetaData*> overlaps)
            if TotalFileSize(overlaps) > 10 * vset_->options_.max_file_size
                return level
    return kMaxMemCompactLevel - 1
> OverlapInLevel(int level, Slice min_key, Slice max_key)
    return Version::SomeFileOverlapsRange(level > 0, files_[level], min_key, max_key)
> GetOverlappingInputs(const int level, InternalKey* begin, InternalKey* end, vector<FileMetaData*>& ret)
    // 获取第level层的文件中有key在[begin, end]上的文件
    Slice user_begin, end_begin = begin.user_key, end.user_key
    for i = 0; i < files_[level].size;
        FileMetaData* f : files_[level][i++]
        Slice file_begin, file_end = f.smallest.user_key, f.largest.user_key
        if file_end < user_begin || file_begin > end_begin
            continue  // f的key都不在begin和end之间
        ret.push_back(f)
        if level == 0 && (user_begin >= file_begin || file_end > user_end)
            // expand [user_begin, user_end]
            user_begin >_ ?= file_begin
            user_end <_ ?= file_end
            ret.clear()
            i = 0  // restart search
//
> Get(ReadOptions opt, LookupKey k, string* value, GetStats* stats)
    FileMetaData* last_file_read = NULL;  int last_file_read_level = -1
    FileMetaData** files;  size_t file_num
    for level = 0:kNumLevels
        vector<FileMetaData*> filesMaybeHas
        if level == 0
            // files_[level == 0]不是有序的, 需要一个个地check
            filesMaybeHas = files_[0].filter(
                (FileMetaData* f) -> k.user_key >= f.smallest.user_key && k.user_key < f.largest.user_key )
            filesMaybeHas.sort((FileMetaData* f1, f2) -> f1.number > f2.number)  // 新的文件排在前面
        else
            // files_[level > 0]是有序的, 所以可以用FindFile()二分查找
            uint32_t index = Version::FindFile(files_[level], k.internal_key)
            if index < file_num && k.user_key < files_[levle][index].smallest.user_key
                filesMaybeHas.push_back(files_[levle][index])
        //
        for FileMetaData* f : filesMaybeHas
            if last_file_read != NULL && stats.seek_file == NULL
                // seek_file将在UpdateStats()里赋给file_to_compact_, 是下一个做compact的文件
                stats.seek_file = last_file_read
                stats.seek_file_level = last_file_read_level
            last_file_read = f
            last_file_read_level = level
            //
            ValueType retType;  Slice retUserKey, retValue
            vset_.table_cache_.Get(opt, f.number, f.file_size, k.internal_key, NULL,
                (void* arg, Slice internal_key, Slice v) ->  // 查询回调函数
                    从 internal_key 解析出 retType, retUserKey;  retValue = v )
            if retUserKey == k.user_key
                if retType == kTypeValue, value = retValue  // 找到了
                else stats = Status::NotFound()             // key被删除了
                return
> UpdateStats(GetStats stats)
    FileMetaData* f = stats.seek_file
    if f != NULL && --f.allowed_seeks <= 0 && file_to_compact_ == NULL
        file_to_compact_ = f
        file_to_compact_level_ = stats.seek_file_level
        return true
    return false