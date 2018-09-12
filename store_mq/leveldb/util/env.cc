静态
> LockOrUnlock(int fd, bool lock)  // 加锁和解锁用同一个函数实现
    errno = 0
    flock f = {0}
    f.l_type = lock ? F_WRLCK : F_UNLCK
    f.l_whence = SEEK_SET
    f.l_start = f.l_len = 0
    return fcntl(fd, F_SETLK, &f)

> DoWriteStringToFile(Env* env, Slice& data, string& fname, bool isSync)
    env->NewWritableFile(fname, out WritableFile* file)
    file->Append(data)
    if isSync, file->Sync()
    file->Close()
    // if !s.ok(), env->DeleteFile(fname)


class Env
// 接口, 具体子类-PosixEnv
> Default():Env* static
    static const Env* default_env = new PosixEnv
    return default_env


class PosixEnv : public Env
> 字段
    PosixLockTable locks_
> CreateDir(string& name):Status virtual
    if mkdir(name.c_str(), 0755) != 0
        return Status::IOError(name, errno)
    return Status::OK()
> LockFile(string& fname, FileLock** lock)
    *lock = NULL
    int fd = open(fname.c_str(), O_RDWR | O_CREAT, 0644)
    if fd < 0
        return Status::IOError(fname, errno)
    if !locks_.Insert(fname)  // 已经被加过锁
        close(fd)
        return Status::IOError("lock "+fname, "already held by process")
    if LockOrUnlock(fd, true) == -1
        close(fd)
        locks_.Remove(fname)
        return Status::IOError("lock"+fname, errno)
    PosixFileLock* l = new PosixFileLock
    l->fd_ = fd
    l->name_ = fname
    *lock = l;
    return Status::OK()
> GetFileSize(string fname, uint64_t* size)
    struct stat sbuf
    stat(fname.c_str, &sbuf)
    *size = sbuf.st_size
> NewSequentialFile(string fname, SequentialFile** ret)
    FILE* f = fopen(fname.c_str, "r")
    *ret = new PosixSequentialFile(fname, f)  // PosixFile.cc
> NewAppendableFile(string fname, WritableFile** ret)
    FILE* f = fopen(fname.c_str, "a")
    *ret = new PosixWritableFile(fname, f)  // PosixFile.cc
> NewWritableFile(string fname, WritableFile** ret)
    FILE* f = fopen(fname.c_str, "w")
    *ret = new PosixWritableFile(fname, f)  // PosixFile.cc


class PosixLockTable
// 管理已经加过锁的文件
> 字段
    port::Mutex mu_
    set<String> locked_files_
> Insert(string& fname):bool
    MutexLock l(&mu_)
    return locked_files_.insert(fname).second
> Remove(string& fname)
    MutexLock l(&mu_)
    locked_files_.erase(fname)