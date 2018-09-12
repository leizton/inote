静态
> Table::Open(Options& opt, RandomAccessFile* file, uint64_t size, Table** table)
    // 打开sst文件
    // @ref TableBuilder.cc
    const int footsize = Footer::kEncodedLength
    file.Read(size-footsize, footsize, out Slice footer_input, out char[footsize] footer_space)
    Footer footer.DecodeFrom(footer_input)
    // read index-block
    ReadBlock(file, opt, footer.index_handle, out BlockContents contents)
    Block* index_block = new Block(contents)
    //
    *table = new Table(opt, file, footer.metaindex_handle, index_block)
    *table.ReadMeta(footer)


class Table
// sst table
> (Options& opt, RandomAccessFile* file, BlockHandle metaindex_handle, Block* index_block)
    opt_(opt)
    file_(file)
    metaindex_handle_(metaindex_handle)
    index_block_(index_block)
    filter_:FilterBlockReader*
    filter_data:char*
> ReadMeta(Footer& footer)
    string key = "filter." + opt_.filter_policy.Name
    ReadBlock(file_, opt_, metaindex_handle_, out BlockContents contents)
    Block* meta = new Block(contents)
    Iterator* it = meta.NewIterator
    it.Seek(key)
    if it.Valid && it.key == key
        this.ReadFilter(it.value)
//
> InternalGet(ReadOptions opt, Slice internal_key, void* arg, void (*saver)(void*, Slice, Slice))
    // 查找所在的index_block
    // it.value是TableBuilder::WriteDataBlock()的data_block_handle
    // it.value.offset记录data_block的offset
    Iterator* it = index_block_.NewIterator
    it.Seek(internal_key)
    if !it.Valid, return
    // 过滤
    if filter_ != NULL
        BlockHandle handle.DecodeFrom(it.value)
        if filter.KeyMayMatch(handle.offset, internal_key) == false
            return  // 不在filter中
    // 读取data_block, 并查找
    Iterator* block_it = this.BlockReader(this, opt, it.value)
    block_it.Seek(internal_key)
    if block_it.Valid
        (*saver)(arg, block_it.key, block_it.value)