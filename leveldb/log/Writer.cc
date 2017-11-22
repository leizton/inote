class Writer
> (WritableFile* dest)  // PosixFile.cc PosixWritableFile
    dest_(dest)
    block_offset_:int
> AddRecord(Slice s)
    auto to_write_ptr = s.data
    size_t left = s.size
    bool begin = true
    while left > 0
        const int leftover = kBlockSize - block_offset_
        if leftover < kHeaderSize
            // 当前block剩下的写空间不够kHeaderSize
            if leftover > 0
                // 用\x00填充block的尾部
                dest_.Append(Slice("\x00\x00\x00\x00\x00\x00\x00", leftover))
            block_offset_ = 0
        //
        const size_t avail = kBlockSize - block_offset_ - kHeaderSize
        const size_t write_size = left < avail ? left : avail
        const bool end = left == write_size
        RecordType type =
            if begin && end,  kFullType
            if begin,         kFirstType
            if end,           kLastType
            else              kMiddleType
        this.EmitPhysicalRecord(type, to_write_ptr, write_size)
        to_write_ptr += write_size
        left -= write_size
        begin = false