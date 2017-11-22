class VersionEdit
> 字段
    has_comparator_(false),        comparator_():string
    has_log_number_(false),        log_number_(0)
    has_prev_log_number_(false),   prev_log_number_(0)
    has_last_sequence_(false),     last_sequence_(0)
    has_next_file_number_(false),  next_file_number_(0)
    //
    compact_pointers_ :vector<pair<int, InternalKey>>
    deleted_files_    :set<pair<int, uint64_t>>
    new_files_        :vector<pair<int, FileMetaData>>
> SetLogNumber(uint64_t num)
    has_log_number_ = true
    log_number_ = num
> DeleteFile(int level, uint64_t file)
    deleted_files_.insert(make_pair(level, file))
> AddFile(int level, FileMetaData f)
    FileMetaData newf(f)
    new_files_.push_back(make_pair(level, newf))


class FileMetaData
> ()
    refs          :int
    allowed_seeks :int(2^30)
    number        :uint64_t     // 文件编号
    file_size     :uint64_t
    smallest      :InternalKey
    largest       :InternalKey