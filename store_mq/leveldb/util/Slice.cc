class Slice
>
    const char* data_
    size_t size_
>
    Slice() :data_(""), size_(0) {}
    Slice(const char* d, size_t n) :data_(d), size_(n) {}
    Slice(const std::string& s) :data_(s.data()), size_(s.size()) {}
>
    data():const char* => data_
    size():size_t => size_
    empty():bool => size_ == 0
    clear() => data_="", size_=0  // 不做delete[] data_
    remove_prefix(size_t n) => data_+=n, size_-=n
    start_with(const Slice& x):bool => size_>=x.size_ && memcmp(data_, x.data_, x.size_)==0
    operator[](size_t n):char => data_[n]