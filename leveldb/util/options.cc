struct Options
> Options()
    comparator(BytewiseComparator())  // comparator.cc
    create_if_missing(false)
    error_if_exists(false)
    paranoid_checks(false)
    env(Env::Default()):Env*  // env.cc
    info_log(NULL)
    write_buffer_size(4<<20)
    max_open_files(1000)
    block_cache(NULL)
    block_size(4096)
    block_restart_interval(16)
    max_file_size(2<<20)
    compression(kSnappyCompression)
    reuse_logs(false)
    filter_policy(NULL)

 
struct WriteOptions
> 字段
    bool sync


struct ReadOptions
> 字段
    bool verify_checksums
    bool fill_cache
    Snapshot* snapshot