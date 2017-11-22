kHeader = 12


class WriteBatch
> WriteBatch()
    rep_:string
    rep_.resize(kHeader)
> InsertInto(MemTable* mem)
    // rep_前8个字节是uint64_t的sequence
    this->Iterate(new MemTableInserter(DecodeFixed64(rep_.data), mem))
> Iterate(Handler* handler)
    Slice input(rep_)
    input.remove_prefix(kHeader)
    while !input.empty
        input.remove_prefix(1)
        if input[0] == kTypeValue
            GetLengthPrefixedSlice(input, out Slice key)
            GetLengthPrefixedSlice(input, out Slice value)
            handler->Put(key, value)
> Put(Slice key, Slice value)
    this.SetCount(this.Count + 1)
    rep_.push_back((char) kTypeValue)
    PutLengthPrefixedSlice(&rep_, key)
    PutLengthPrefixedSlice(&rep_, value)
> Append(WriteBatch* b)
    // 把b的rep_追加到自身的rep_后面
    SetCount(this.Count + b.Count)
    rep_.append(b.rep_.data + kHeader, b.rep_.size - kHeader)
//
> Sequence():uint64_t = DecodeFixed64(rep_.data)
> SetSequence(uint64_t seq) = EncodeFixed64(rep_, seq)
> Count():int = DecodeFixed32(rep_.data + 8)
> SetCount(int n) = EncodeFixed32(rep_.data + 8, n)
> Contents():Slice = Slice(rep_)


class MemTableInserter : WriteBatch::Handler
> MemTableInserter(uint64_t sequence, MemTable* mem)
    sequence_(sequence)
    mem_(mem)
> Put(Slice key, Slice value)
    mem_->Add(sequence_, kTypeValue, key, value)
    ++sequence_