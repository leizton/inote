# 当前目录大小，-h 人性化文件大小，-s 只显示单个结果,对于目录是不会展开的
$ du -h -s /Users/whiker/bin(不填写则选取当前目录)

$ ls | xargs du -h -s