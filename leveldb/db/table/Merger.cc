静态
> Merger::NewMergingIterator(Iterator** list, int n):Iterator*
    if n == 0, => NewEmptyIterator()
    if n == 1, => list[0]
    => new MergingIterator(list, n)


class MergingIterator
// 多路归并
> (Iterator** children, int n)
    children_:IteratorWrapper* = new IteratorWrapper[n] { children[0:n] }
    n_(n)
    current_(NULL):IteratorWrapper*
    direction_(kForward):enum Direction{ kForward, kReverse }
> Valid() = current_ != NULL
> key()   = current_.key
> value() = current.value
> FindSmallest()
    IteratorWrapper* smallest = children_[0]
    for child : children_[1:]
        if child.Valid && child.key < smallest.key
            smallest = child
    current_ = smallest
> SeekToFirst()
    for child : children_
        child.SeekToFirst()
    FindSmallest()
    direction_ = kForward
> Next()
    if direction_ != kForward
        for child : children_
            if child == current_
                child.Next()
            else
                child.Seek(this.key)
                if child.Valid && child.key == this.key
                    child.Next()
    FindSmallest()  // 从children_中找最小key的child, 赋给current_