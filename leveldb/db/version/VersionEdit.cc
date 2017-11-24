class VersionEdit
> 字段
    has_comparator_(false),        comparator_():string
    has_log_number_(false),        log_number_(0)
    has_prev_log_number_(false),   prev_log_number_(0)
    has_last_sequence_(false),     last_sequence_(0)
    has_next_file_number_(false),  next_file_number_(0)
    // 以下pair的first是level
    compact_pointers_ :vector<pair<int, InternalKey>>
    deleted_files_    :set<pair<int, uint64_t>>
    new_files_        :vector<pair<int, FileMetaData>>
> SetLogNumber(uint64_t num)
    has_log_number_ = true
    log_number_ = num
> SetCompactPointer(int level, InternalKey& key)
    compact_pointers_.push_back(make_pair(level, key))
> DeleteFile(int level, uint64_t file)
    deleted_files_.insert(make_pair(level, file))
> AddFile(int level, FileMetaData f)
    FileMetaData newf(f)
    new_files_.push_back(make_pair(level, newf))
> EncodeTo(out string dst)
    if has_comparator_
        PutVarint32(dst, kComparator)  // kComparator是标识
        PutLengthPrefixedSlice(dst, comparator_)
    // 省略 log_number_ prev_log_number_ last_sequence_ next_file_number_
    for auto p : compact_pointers_
        PutVarint32(dst, kCompactPointer)
        PutVarint32(dst, p.first)  // level
        PutLengthPrefixedSlice(dst, p.second.Encode())
    // 省略 deleted_files_ new_files_


class FileMetaData
> ()
    refs          :int
    allowed_seeks :int(2^30)
    number        :uint64_t     // 文件编号
    file_size     :uint64_t
    smallest      :InternalKey
    largest       :InternalKey