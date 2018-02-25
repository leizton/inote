* b-tree即balance tree。
* b+tree和b-tree的区别：b+tree的非叶节点只存key不存data，叶子节点包含指向右侧兄弟节点的指针。
* b+tree的一个非叶节点可以存更多的key，所以b+tree的度更大树高更小。
* b+tree树高更小，所以查询时需要的磁盘IO次数更少，b+tree更适合于外部存储(非内存)。
* mysql的索引文件一般都很大，主要存在磁盘上。myIsam使用b-tree，innoDb使用b+tree，innoDb要求每张表必须包含唯一主键(即b+tree叶节点的key)。
* b+tree的一个节点物理大小设成磁盘的一页(4KB)，这样一次IO就读一个完整的节点。