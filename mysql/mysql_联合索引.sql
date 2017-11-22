-- 建表语句
CREATE TABLE `table_1` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='table_1';

-- 添加联合索引
ALTER TABLE table_1 ADD INDEX idx_name_create_time (`create_time`, `name`);
explain select * from table_1 where name='020' and create_time between '2017-01-10 22:52:06' and '2017-01-11 03:32:06'\G
-- 需要扫描27行
           id: 1
  select_type: SIMPLE
        table: table_1
         type: range
possible_keys: idx_name_create_time
          key: idx_name_create_time
      key_len: 406
          ref: NULL
         rows: 27
        Extra: Using where; Using index

-- 改变联合索引顺序
ALTER TABLE table_1 DROP INDEX idx_name_create_time;
ALTER TABLE table_1 ADD INDEX idx_name_create_time (`name`, `create_time`);
explain select * from table_1 where name='020' and create_time between '2017-01-10 22:52:06' and '2017-01-11 03:32:06'\G
-- 只需扫描1行
           id: 1
  select_type: SIMPLE
        table: table_1
         type: range
possible_keys: idx_name_create_time
          key: idx_name_create_time
      key_len: 406
          ref: NULL
         rows: 1
        Extra: Using where; Using index