# 把查询结果输出到文件
## mysql在本机
select * from {table} into outfile '/tmp/a.txt';
## mysql在远程机
mysql -u"Username" -p"Password" -D"Database" -h"host域名或IP" -P"端口,默认3306" -e"sql语句" > '/tmp/a.txt'


# 从 table1 导数据到 table2
## 两张表在同一db
insert into table2 select * from table1;
insert into table2 (field1) select field1 from table1;
## 两张表不在同一db
用 mysqldump 生成sql脚本
mysqldump --skip-add-drop-table -u{user} -p{pwd} -h{host} -P{port} --default-character-set=utf8 --databases {db} --tables {table} --where="type='test'" > tmp.sql
mysql -u{user} -p{pwd} -h{host} -P{port} -D{db} --default-character-set=utf8 < tmp.sql
`tips`
	--default-character-set=utf8，必须写上，否则容易编码出问题
	类似 /*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */; 用于保留旧值的语句可以删除
`mysqldump options`:
	--skip-add-drop-table:    不添加删除表的sql语句, 默认是会加删除原表语句
	--tables:                 不加该选项时导出所有表
	--skip-lock-tables:       出现'mysqldump: Got error: 1044: Access denied for user'时, 加上
	--no-create-info:         不创建表
	--single-transaction:     当lock语句不允许执行时, 加该选项则没有'LOCK TABLE'语句