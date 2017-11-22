静态
> MakeFileName(string dbname, uint64_t num, char* suffix)
    return dbname + "%06llu"{num} + suffix
> TempFileName(string& dbname, uint64_t num)
    return dbname + "%06llu"{num} + "dbtmp"
> DescriptorFileName(string& dbname, uint64_t num)
    return dbname + "/MANIFEST-" + "%06llu"{num}
> CurrentFileName(string& dbname)
    return dbname + "/CURRENT"

> SetCurrentFile(Env* env, string dbname, uint64_t descriptor_num)
    // 构造manifest文件名: MANIFEST-000001
    Slice manifestFileName = DescriptorFileName(dbname, descriptor_num)
    manifestFileName.remove_prefix(dbname.size() + 1)
    // 创建一个临时文件
    string tmpName = TempFileName(dbname, descriptor_num)
    // 把manifest文件名写入临时文件
    DoWriteStringToFile(env, manifestFileName+"\n", tmpName, true)  // env.cc
    // 把临时文件改成current文件
    env->RenameFile(tmpName, CurrentFileName(dbname))