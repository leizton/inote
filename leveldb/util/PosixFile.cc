class SequentialFile
    Read(size_t n, Slice* result, char* assist)
    Skip(uint64_t n)
class WritableFile
    Append(Slice&)
    Close()
    Flush()
    Sync()


class PosixSequentialFile : SequentialFile
> PosixSequentialFile(string fname, FILE* f)
    filename_(fname)
    file_(f)
> ~PosixSequentialFile() virtual
    fclose(f)
> Read(size_t n, Slice* result, char* assist)
    size_t r = fread(assist, 1, n, file_)  // stdio.h
    *result = Slice(assist, r)
> Skip(uint64_t n)
    // fcntl.h里定义SEEK_CUR是从当前位置开始seek
    fseek(file_, n, SEEK_CUR)  // stdio.h


class PosixWritableFile : WritableFile
> PosixWritableFile(string& fname, FILE* f)
    filename_(fname)
    file_(f)
> Append(Slice& data)
    fwrite_unlocked(data.data(), 1, data.size, file_)
> Flush()
    fflush_unlocked(file_)