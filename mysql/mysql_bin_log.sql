三种模式
	1. Row (基于行)
		对于一条sql语句, 记录它修改了哪些行以及这些行改后的数据.
		优点: 不易出错, 可做到原样复制;
		缺点: 数据量可能很大, 例如delete from t1 where type=1或alter table修改了很多行, 则需记录大量数据.
	2. Statement (基于语句)
		记录会修改数据的sql语句
		优点: 数据量小;
		缺点: 很难做到主从一致, 例如rand()/uuid()等在不同地方返回值不同.
	3. Mixed (混合)
		不会产生歧义的sql语句用Statement模式, 会产生歧义的用Row模式.
	4. 设置模式
		1. 配置文件(/etc/mysql/my.cnf)中设置, binlog_format = ROW/STATEMENT/MIXED;
		2. mysql> set binlog_format = 'STATEMENT';  -- 仅当前数据库
		   mysql> set global binlog_format = 'STATEMENT';  -- 所有数据库

在配置文件中设置
log_bin = /var/log/mysql/mysql-bin.log
binlog_format = STATEMENT

> flush logs;  -- 生成新的日志文件
> truncate table t1;
> show binlog events;
+------------------+-----+-------------+-----------+-------------+------------------------------------------------+
| Log_name         | Pos | Event_type  | Server_id | End_log_pos | Info                                           |
+------------------+-----+-------------+-----------+-------------+------------------------------------------------+
| mysql-bin.000001 |   4 | Format_desc |         1 |         107 | Server ver: 5.5.35-1ubuntu1-log, Binlog ver: 4 |
| mysql-bin.000001 | 107 | Query       |         1 |         193 | use `binlogtest`; truncate table t1            |
+------------------+-----+-------------+-----------+-------------+------------------------------------------------+

> insert into t1(tag) values('line a');
> insert into t1(tag) values('line b');
> update t1 set tag='line a1' where id=1;
> show binlog events;
+------------------+-----+-------------+-----------+-------------+----------------------------------------------------------+
| Log_name         | Pos | Event_type  | Server_id | End_log_pos | Info                                                     |
+------------------+-----+-------------+-----------+-------------+----------------------------------------------------------+
| mysql-bin.000001 |   4 | Format_desc |         1 |         107 | Server ver: 5.5.35-1ubuntu1-log, Binlog ver: 4           |
| mysql-bin.000001 | 107 | Query       |         1 |         193 | use `binlogtest`; truncate table t1                      |
| mysql-bin.000001 | 193 | Query       |         1 |         267 | BEGIN                                                    |
| mysql-bin.000001 | 267 | Intvar      |         1 |         295 | INSERT_ID=1                                              |
| mysql-bin.000001 | 295 | Query       |         1 |         400 | use `binlogtest`; insert into t1(tag) values('line a')   |
| mysql-bin.000001 | 400 | Xid         |         1 |         427 | COMMIT /* xid=130 */                                     |
| mysql-bin.000001 | 427 | Query       |         1 |         501 | BEGIN                                                    |
| mysql-bin.000001 | 501 | Intvar      |         1 |         529 | INSERT_ID=2                                              |
| mysql-bin.000001 | 529 | Query       |         1 |         634 | use `binlogtest`; insert into t1(tag) values('line b')   |
| mysql-bin.000001 | 634 | Xid         |         1 |         661 | COMMIT /* xid=131 */                                     |
| mysql-bin.000001 | 661 | Query       |         1 |         735 | BEGIN                                                    |
| mysql-bin.000001 | 735 | Query       |         1 |         842 | use `binlogtest`; update t1 set tag='line a1' where id=1 |
| mysql-bin.000001 | 842 | Xid         |         1 |         869 | COMMIT /* xid=133 */                                     |
+------------------+-----+-------------+-----------+-------------+----------------------------------------------------------+

# mysqlbinlog mysql-bin.000001
(其中的一条insert语句)
...
# at 193
#160317 16:17:21 server id 1  end_log_pos 267 	Query	thread_id=39	exec_time=0	error_code=0
SET TIMESTAMP=1458202641/*!*/;
BEGIN
/*!*/;
# at 267
#160317 16:17:21 server id 1  end_log_pos 295 	Intvar
SET INSERT_ID=1/*!*/;
# at 295
#160317 16:17:21 server id 1  end_log_pos 400 	Query	thread_id=39	exec_time=0	error_code=0
SET TIMESTAMP=1458202641/*!*/;
insert into t1(tag) values('line a')
/*!*/;
# at 400
#160317 16:17:21 server id 1  end_log_pos 427 	Xid = 130
COMMIT/*!*/;
...

生成sql脚本
# mysqlbinlog mysql-bin.000001 --start-datetime="2016-03-17 16:17:00" --stop-datetime="2016-03-17 16:18:00" > a.sql
  或者 mysqlbinlog mysql-bin.000001 --start-position=4 --stop-position=869 > a.sql
  --start-datetime 设置开始时间, 可以不是日志里出现的时间
  --stop-datetime  设置结束时间
  --start-position 开始位置, 必须是日志类at的pos
  --stop-position  结束位置, 不包括该条

用sql脚本恢复数据
$ mysql -uroot -p -Dbinlogtest<a.sql