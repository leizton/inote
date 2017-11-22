-- 用脚本启动和停止
	$ sudo /etc/init.d/mysql start(stop restart)

数据库的磁盘文件 /var/lib/mysql
	mysql> show variables like 'datadir';
	查看当前server的data路径, 一般是/var/lib/mysql
	存放各个数据库的磁盘文件

配置文件位置 /etc/mysql/my.conf
	$ whereis mysqld
	mysqld: /usr/sbin/mysqld /usr/share/man/man8/mysqld.8.gz
	$ /usr/sbin/mysqld --verbose --help | grep -A 1 'Default options'
	/etc/my.cnf /etc/mysql/my.cnf /usr/etc/my.cnf ~/.my.cnf (最后一行)

日志文件 /var/log/mysql/mysql-bin.*

$ mysql -u用户名 -p密码 -D数据库名称<SQL脚本路径(/home/whiker/test.sql)

数据类型
	整数 tinyint smallint mediumint int bigint, 字节数 1 2 3 4 8
	浮点数 float(M,D) double(M,D) M总位数 D小数点后位数
	字符串 char(0~255)            varchar(0~65535) 
		   tinytext(2^8=256)      text(2^16=64KB)
		   mediumtext(2^24=16MB)  longtext(2^32=1GB)
	日期 year time date datetime timestamp
	枚举和集合 enum set
	定点数 decimal(M,D), M是整数加小数一起的位数, D是小数部分的位数
		create table t1(price decimal(5,2));
		insert into t1 values(0.1), (100.01), (100.014), (100.015), (1000.00);
			-- 实际存入0.1 100.01 100.01 100.02 999.99

3个范式
	表的每列(字段)是不可分割的原子数据
	表的每行(实体/记录)可被唯一区分
	表中不包含其它表的非主键字段

$ mysql -u root -p
mysql>

> show engines;  -- 支持的引擎
> show variables like '%storage_engine%';  -- 输出的'default_storage_engine'是当前默认引擎

> select version()版本 now()当前时间;
> select user()当前用户 database()当前数据库;

> show databases;
> create database d1 character set=gbk;
> show create database d1;  -- 显示创建d1时的语句
> alter database d1 character set=utf8;
> drop database d1;

> show tables from d1;
create table t1 (  -- 创建表
	id int not null auto_increment primary key,
	name varchar(20) not null unique key,
	age tinyint unsigned,
	gender enum('0', '1', '2') default '0'  -- 默认0表示未指定性别
);
> show columns from t1;

insert into t1(name, age, gender) values('a', 20, '1');
insert into t1(name, age, gender) values('a', 20, '1'), ('b', 20, default);
insert into t1(name) values('a');  -- 该记录的age是NULL

select * from t1;
select name,age form t1;
select name as username, age from t1;  -- 字段别名
select name from t1 group by gender;  -- 分组, gender每个值只出现一行
select gender from t1 group by gender having count(id)>3;  -- 带条件的分组, 人数大于3的性别
select name from t1 order by age asc;  -- 排序, asc升序, desc降序
select name from t1 order by age asc, id desc;  --多个字段的排序
select name from t1 limit m,n;  -- 返回第m+1条记录开始, 共n条
select distinct(age) from t1;

内联结, select * from t1 a join t2 b on a.name=b.username;
	满足条件的记录(行)输出
左外联结, select * from t1 a left join t2 b on a.name=b.username;
	满足条件的记录(行)输出, 左表(t1)不满足条件的记录(行)也输出, 缺少字段是null
右外联结, select * from t1 a right join t2 b on a.name=b.username;
	满足条件的记录(行)输出, 右表(t1)不满足条件的记录(行)也输出, 缺少字段是null

delete from t1 where id>1;

truncate table t1;  -- 全表数据删除, 性能比delete from t1高

DDL语句, 都是原子操作, 不可回滚
alter table t1 add column city varchar(50);  -- 表的增加字段
alter table t1 modify column city varchar(30);
alter table t1 change column city city varchar(30);
alter table t1 drop column city;  -- 表的删除字段
drop table t1;  -- 彻底删除表t1

MySQL存储引擎: MyISAM InnoDB 等
MyISAM相比InnoDB的缺点:
	MyISAM不支持事务
	MyISAM表级锁, 并发能力差, 而InnoDB行级锁
	没有MVCC功能, MVCC是Multi-Version Concurrent Control多版本并发控制, 快照读
		MVCC保存某个时间点的快照, 使同一事务中看到一致的数据视图, 避免加锁
	MyISAM只能缓存索引, InnoDB能缓存索引和数据
MyISAM由于不支持事务, 使其insert速度比InnoDB快

MyISAM型的表t2, t2.frm t2.MYD(存放数据) t2.MYI(存放索引)

-- id从1到100000
方式1
delete from t1 where user='Sean' and id between 1 and 5000;
delete from t1 where user='Sean' and id between 5001 and 10000;
...
delete from t1 where user='Sean' and id between 95001 and 100000;
方式2
delete from t1 where user='Sean';
哪种更好?
-- create table t1(id int not null auto_increment primary key, name varchar(20), age tinyint default 0);
-- 没有对name建索引
'Sean'记录占1/80
方式1, 用时 0.16 sec, 测试如下:
mysql> delimiter $  -- 设置结束符是$
mysql> create procedure p1()
     > begin
     > declare i,j int;
     > set i=0; set j=5000;
     > while i<100000 do
     >   delete from t1 where name='Sean' and id between i and j;
     >   set i=i+5000; set j=j+5000;
     > end while;
     > end;$
mysql> delimiter ;
mysql> call p1();
方式2, 用时 0.05 sec

聚集索引(主键)
	叶节点包括主键列值 非主键列值 事务ID 回滚指针
	直接在Cluster B+ Tree上查
辅助索引(非主键)
	先在Secondary B+ Tree查出主键(二级索引的叶节点包含主键值), 再用主键在Cluster B+ Tree上查

联合索引字段整体有序, 左前缀字段有序
联合索引可代替左前缀字段的单列索引
查询条件中联合索引的左前缀是确定值(where中的等号), 另一个字段是有序的

覆盖索引
	索引包含所有要查询的字段的值
	索引叶节点存储了select数据列的数据, 直接从索引就可以得到数据

用不到索引的情况
	where中没内容
	否定条件: <>, not in, not exists
	join中联结字段类型不一致
	扫描内容超过全表的20%(此时用全表查找不会慢很多, 而又省去了索引过程)
	where条件的字段存在函数运算(mysql不支持函数索引)
	like '%name'(不在开头的, %name%在中间的索引也没用, 因为mysql不是全文索引)
	出现隐式字符类型转换

访问效率: const(主键) > eq_ref(唯一索引) > ref(索引扫描,可能有多个匹配值)
			> range(给索引定了范围) > index(全索引) > all(全表)

查看执行计划
mysql> explain select name from t1 where gender='0';
	possible_kyes(可能用的索引), key(最终用到的索引), key_len, rows(估计要扫描的行数)

外联结中存在where, 会使外联结变成内联结
> create table product(id int not null auto_increment primary key, amount int default 0);
> insert into product(amount) values(100),(200),(300);
> create table product_detail(id int not null auto_increment primary key, weight int default 0);
> insert into product_detail(weight) values(25);
> select * from product a left join product_detail b on a.id=b.id and b.id=1;
| id | amount | id   | weight |
+----+--------+------+--------+
|  1 |    100 |    1 |     25 |
|  2 |    200 | NULL |   NULL |
|  3 |    300 | NULL |   NULL |
左外联结的记录在右表中找不到匹配记录时, 也会输出, 如第2 3行.
> select * from product a left join product_detail b on a.id=b.id where b.id=1;
| id | amount | id | weight |
+----+--------+----+--------+
|  1 |    100 |  1 |     25 |
用了where后就不是左外联结

事务特征: ACID
Atomicity   原子性, 事务中的所有操作要么都执行成功, 要么都不生效, 执行过程中有错误会回滚
Consistency 一致性, 指数据的一致性, 例如执行前表Product的apple记录的type字段是表Type的水果id,
					执行后apple的type设成电子产品, 然而表Type里没有电子产品这个记录,
					结果是表Product存在记录的type不对应表Type中的任一记录, 这里出现了数据不一致.
					关系型数据库是保持强一致性.
Isolation   隔离性, 事务可以认为当前只有自己在使用系统, 不受其他事务的干扰
Durability  持久性, 成功的事务作出的修改是持久保存在数据库中

mysql>begin;  >insert into t1(name) values('a');  >insert into t1(name) values('a'); >commit;  -- 两条插入被提交
mysql>begin;  >insert into t1(name) values('c');  >rollback;  -- rollback使这条插入不生效

索引
	alter table user add unique uniq_user_id (user_id);  -- 创建唯一索引, uniq_user_id是索引名, user_id是列名
	alter table user add index idx_user_name (user_name);  -- 创建索引
	alter table user drop index idx_user_name;  -- 删除索引
	show index from user;  -- 查看表user的索引