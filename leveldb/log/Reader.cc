enum RecordType
    kZeroType    // 用于文件预分配
    kFullType    // 这是一个完整的record
    kFirstType   // 这是一个record的第一个record
    kMiddleType
    kLastType
    kEof
    kBadRecord


class Reader
// 一个Block中有多个Record, 一个Record可能跨越多个Block
> Reader(SequentialFile* file, Reporter* /*仅用于传递status*/, bool checksum, uint64_t init_offset)
    file_(file):SequentialFile*
    backing_store_(new char[kBlockSize]):char*
    buffer_(""):Slice
    eof_(false)
    last_record_offset_(0)
    end_of_buffer_offset_(0)

> ReadRecord(Slice* record, string* assist):bool
    record.clear(), assist.clear()
    uint64_t first_record_offset = 0
    while 1
        // 读一次文件
        uint record_type = ReadPhysicalRecord(out Slice fragment)
        // physical_record_offset是本次record在整个文件的偏移
        // first_record_offset是读到的第一个record的物理偏移
        uint64_t physical_record_offset = end_of_buffer_offset_ - (kHeaderSize+fragment.size()) - buffer_.size()
        switch record_type
            case kFullType:
                last_record_offset_ = first_record_offset = physical_record_offset
                *record = fragment, assist.clear()
                return true
            case kFirstType:
                first_record_offset = physical_record_offset
                assist.assign(fragment.data(), fragment.size())
            case kMiddleType:
                assist.append(fragment.data(), fragment.size())
            case kLastType:
                last_record_offset_ = first_record_offset
                assist.append(fragment.data(), fragment.size())
                *record = Slice(assist)
                return true
            case kEof:
                return false

> ReadPhysicalRecord(Slice* result):uint private
    // header: checksum(4 bytes) recordLen(2) type(1)
    while buffer_.size() < kHeaderSize
        buffer_.clear()
        if eof_, return kEof
        file_.Read(kBlockSize, &buffer_, backing_store_)  // 读取kBlockSize个字节
        end_of_buffer_offset_ += buffer_.size()
        eof_ = buffer_.size() < kBlockSize  // 实际读到的少于kBlockSize个字节, 所以读到了文件末尾
    // parse header
    const char* header = buffer_.data()
    uint32_t recordLen = (header[5]<<8) + header[4]
    if recordLen > buffer_.size() - kHeaderSize
        buffer_.clear()
        return eof_ ? kEof : kBadRecord
    uint type = header[6]
    if type == kZeroType && recordLen == 0
        buffer_.clear()
        return kBadRecord
    buffer_.remove_prefix(kHeaderSize + recordLen)  // 清除本次读出的record
    *result = Slice(header + kHeaderSize, recordLen)
    return type