namespace config
// 定义数据库的全局常量

// db.cc
kNumNonTableCacheFiles = 10

// record
kHeaderSize = 7
kBlockSize = 32768

kNumLevels = 7
kL0_CompactionTrigger = 4
kMaxMemCompactLevel = 2

kL0_SlowdownWritesTrigger = 8

// cache
kNumShardBits = 4
kNumShards = 1 << kNumShardBits