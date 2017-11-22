$ find . -name "*.csv" | xargs -i cp {} ~/data
# 把当前目录下的所有csv文件复制到~/data目录下
# xargs的-i选项把前面的结果输出到{}位置