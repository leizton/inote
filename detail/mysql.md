# 登录
mysql -D{db} -h${host} -P3306 -u${username} -p${password}

# 命令行执行一条查询语句, 把结果保存到文件
`-e "查询语句"`
mysql -D{db} -h${host} -P3306 -u${username} -p${password} -e"select * from t where id=0;" > ~/a

# 执行sql脚本文件
`< a.sql`
mysql -D{db} -h${host} -P3306 -u${username} -p${password} < ~/a.sql

# binlog文件
`-p binlog文件名`
`-d 筛选出某个数据库`
`--start-datetime="起始时间"`
`-v -v --base64-output=DECODE-ROWS 解码base64`
`> a.sql 生成sql脚本`
mysqlbinlog -p mysql-bin.000001 -d${dbname} --start-datetime="2000-01-01 00:00:00" -v -v --base64-output=DECODE-ROWS > a.sql

# 一条update语句在binlog里的内容
`update table_name set col2='c2_v2' where id=1001;`
### UPDATE `db_name`.`table_name`
### WHERE
###   @1=1001 /* 主键 */
###   @2='c2_v1' /* 第2列 */
###   @3='c3_v1' /* 第3列 */
### SET
###   @1=1001 /* 主键 */
###   @2='c2_v2' /* 第2列 */
###   @3='c3_v1' /* 第3列 */