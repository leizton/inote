struct CompactionStats
// 每个level的压缩状态
> 字段
    micros int64_t
    bytes_read int64_t
    bytes_written int64_t
> Add(CompactionStats& stats)
    micros += stats.micros
    bytes_read += stats.bytes_read
    bytes_written += stats.bytes_written