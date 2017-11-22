class Compaction
> (Options opt, int level)
    level_(level)
    max_output_file_size_(MaxFileSizeForLevel(opt, level))
    input_version_:Version*
    edit_:VersionEdit
    inputs_[2]:vector<FileMetaData*>
    grandparents_:vector<FileMetaData*>
    grandparent_index_:size_t
    seen_key_:bool
    overlapped_bytes_:int64_t
    level_ptrs_[kNumLevels]:size_t = {0}
> IsTrivialMove()
    = inputs_[0].size == 1 && inputs_[1].size == 0 &&
      Version::TotalFileSize(grandparents_) <= 10 * input_version_.vset_.options_.max_file_size
> IsBaseLevelForKey(Slice user_key)
    for level_i = (level_+2):kNumLevles
        vector<FileMetaData*>& files = input_version_.files[level_i]
        for file_i : level_ptrs_[level_i]:files.size
            FileMetaData* f = files[file_i]
            if user_key >= f.smallest.user_key && user_key <= f.largest.user_key
                level_ptrs_[level_i] = file_i
                return false  // user_key在f中
        level_ptrs_[level_i] = files.size
    return true


class CompactionState
> (Compaction* c)
    compaction(c)
    smallest_snapshot:uint64_t  // seq
    outputs:vector<Output>
    outfile:WritableFile*
    builder:TableBuilder*  // sst builder
    total_bytes:uint64_t
> current_output():Output*
    = &outputs[outputs.size - 1]


class Output
> (uint64_t file_number)
    number(file_number)
    file_size:uint64_t
    smallest, largest:InternalKey