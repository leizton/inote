class TableBuilder
// 写sst文件, 后缀.ldb
> (Options& opt, WritableFile* f)
    options_(opt)
    data_block_(opt):BlockBuilder  // 数据块
    index_block_(opt):BlockBuilder  // 索引块
    file_(f)
    offset_:uint64_t
    num_entries_:int64_t
    last_key_:string
    filter_block_(new FilterBlockBuilder)  // 布隆过滤器, 提高查询速度
> Add(Slice key, Slice value)
    // 把kv添加到data_block
    last_key_ = key
    num_entries_++
    data_block_.Add(key, value)
    filter_block_.AddKey(key)  // 把key放入filter_block
    //
    if data_block_.CurrentSizeEstimate >= options_.block_size
        WriteDataBlock()
> WriteDataBlock()
    WriteBlock(&data_block_, out BlockHandle data_block_handle)
    index_block_.Add(last_key_, data_block_handle.Encode())  // 添加新的index_block
> WriteBlock(BlockBuilder* block, BlockHandle& handle)
    Slice raw = block.Finish()  // 获取block的内容
    block.Reset()  // 把data_block还原成构造之初, 方便下次写入
    // 记录block在文件中的偏移和大小
    handle.set_offset_and_size(offset_, raw.size)
    // 把block的内容写入文件
    file_.Append(raw)
    // 把block的trailer写入文件
    file_.Append(char trailer[5] { CompressionType, crc(raw) })  // 压缩类型和crc32
    file_.Flush()
    // 更新offset
    offset_ += content.size + kBlockTrailerSize
> Finish()
    WriteDataBlock()
    // 写入filter_block
    WriteBlock(&filter_block_, out BlockHandle filter_block_h)
    // 写入meta_block, meta_block用来索引filter_block
    BlockBuilder meta_block(options_).Add("filter."+filter_block_.filter_policy, filter_block_h.Encode())
    WriteBlock(&meta_block, out BlockHandle meta_block_h)
    // 写入index_block
    WriteBlock(&index_block_, out BlockHandle index_block_h)
    // footer
    Footer footer(meta_block_h, index_block_h)
    file_.Append(footer.Encode())
    offset_ += footer.size


class BlockBuilder
> (Options opt)
    options_(opt)
    buffer_:string
    restarts_:vector<uint32_t>  // 记录每个start的偏移
    add_counter_:int
    last_key_:string
> Add(Slice key, Slice value)
    size_t shared = 0  // 当前key和上一个key的可共享长度
    if add_counter_ < options_.block_restart_interval
        size_t min_len = min(last_key_.size, key.size)
        while shared < min_len && last_key_[shared] == key[shared]
            ++shared
    else
        restarts_.push_back(buffer_.size)
        add_counter_ = 0
    size_t non_shared = key.size - shared
    // shared | non_shared | value_size
    PutVarint32(&buffer_, shared)
    PutVarint32(&buffer_, non_shared)
    PutVarint32(&buffer_, value.size)
    // delta key | value
    buffer_.append(key.data + shared, non_shared)
    buffer_.append(value.data, value.size)
    // 更新状态
    last_key_.resize(shared)
    last_key_.append(key.data + shared, non_shared)
    ++add_counter_
> Finish()
    // 把所有start的偏移写入到buffer_, 并返回buffer_
    for uint32_t start_offset : restarts_
        PutVarint32(&buffer_, start_offset)
    PutVarint32(&buffer_, restarts_.size)
    return Slice(buffer_)