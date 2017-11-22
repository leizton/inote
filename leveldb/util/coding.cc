静态
> EncodeVarint32(char* dst, uint32_t v):char*
    unsigned char* p = reinterpret_cast<unsigned char*>(dst)
    static const int B = 128
    if v < (1<<7)
        *(p++) = v
    else if v < (1<<14)
        *(p++) = v|B;/*最低7位*/  *(p++) = (v>>7);
    else if v < (1<<21)
    else if v < (1<<28)
    else
        *(p++) = v|B;  *(p++) = (v>>7)|B;  *(p++) = (v>>14)|B;  *(p++) = (v>>21)|B;  *(p++) = (v>>28);
    return reinterpret_cast<char*>(p);
> PutVarint32(string* dst, uint32_t v)
    char buf[5]
    char* ptr = EncodeVarint32(buf, v)
    dst->append(buf, ptr - buf)
> PutLengthPrefixedSlice(string* dst, Slice& v)
    // 先写入v的长度, 再写入内容
    PutVarint32(dst, v.size())
    dst->append(v.data(), v.size())