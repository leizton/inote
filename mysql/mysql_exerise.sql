图书表: id book_id author_id book_name pages press
奖项表: id book_id author_id cup_type cup_time
作者表: id author_id author_name content


一、设计表，写出建表语句
create table `book` (
  `id` int unsigned not null auto_increment comment '主键',
  `book_id` varchar(20) not null default '' comment '图书ID',
  `author_id` varchar(20) not null default '' comment '作者ID',
  `book_name` varchar(30) not null default '' comment '图书名',
  `pages` int unsigned not null default 0 comment '图书页数',
  `press` varchar(40) not null default '' comment '出版社',
  primary key (`id`)
  ) engine=innodb default charset=utf8mb4 comment '图书表';

-- 奖项表
create table `award` (
  `id` int unsigned not null auto_increment comment '主键',
  `book_id` varchar(20) not null default '' comment '图书ID',
  `author_id` varchar(20) not null default '' comment '作者ID',
  `cup_type` tinyint unsigned not null default 0 comment '奖项类型',
  `cup_time` date not null default '1001-01-01 00:00:00' comment '获奖时间',
  primary key (`id`)
  ) engine=innodb default charset=utf8mb4 comment '奖项表';

-- 作者表
create table `author` (
  `id` int unsigned not null auto_increment comment '主键',
  `author_id` varchar(20) not null default '' comment '作者ID',
  `author_name` varchar(30) not null default '' comment '作者姓名',
  `content` varchar(100) not null default '' comment '作者简介',
  primary key (`id`)
  ) engine=innodb default charset=utf8mb4 comment '作者表';


二、设计索引,写出创建索引的语句
-- 图书表的索引
alter table book add unique uniq_book_id (book_id),
  add index idx_author_id (author_id);
-- 奖项表的索引
alter table award add index idx_book_id (book_id),
  add index idx_author_id (author_id),
  add index idx_cup_time (cup_time);
-- 作者表的索引
alter table author add unique uniq_author_id (author_id),
  add index idx_author_name (author_name);


三、完成以下SQL
1. 查询姓王的作者有多少
  select count(*) from author where author_name like '王%';

2. 查询获奖作者总人数
  select count(distinct author_id) from award;

3. 查询同时获得过金奖、银奖的作者姓名
  -- 金奖的cup_type=1, 银奖的cup_type=2
  select a.author_name from author a
    join (select g.author_id
      from (select author_id from award where cup_type=1) g
      join (select author_id from award where cup_type=2) s
      on g.author_id = s.author_id group by g.author_id
    ) gs
    on a.author_id = gs.author_id;

4. 查询获得过金奖的图书有多少本，银奖的有多少本
  select cup_type,count(distinct book_id) from award group by cup_type having cup_type in (1,2);

5. 查询最近一年内获过奖的作者姓名
  select a.author_name from author a join award b on a.author_id=b.author_id
    where b.cup_time between current_date() - interval 1 year and current_date();


四、
1. 如何查看表的结构信息？
  show create table TableName;
  select columns from TableName;

2. 联合索引中的字段顺序应该如何设计？
  联合索引是左前缀有序, 兼顾查询类型和查询频率, 查询频率高的放左边.

3. int(10)和varchar(10)两个字段的(10)有什么区别？
  int(10)中的10表示最大显示宽度, 例如'3000'的显示结果是0000003000,
  varchar(M), M表示最大字符数, 不是字节数, 可以存下M个汉字.

4. 以下查询如何创建索引能够实现覆盖索引优化？(请给出具体SQL)
select invalid_time_flag from pushtoken_android_62 where uid = 'AC54E24E-FB73-3981-C4BC-CED8D69407F8' and pid = '10010'
alter table pushtoken_android_62 add index idx_pid_uid_invalid_time_flag(uid,pid,invalid_time_flag); -- 用了覆盖索引

select count(*) from pushtoken_android_62 where uid = 'AC54E24E-FB73-3981-C4BC-CED8D69407F8' and pid = '10010'
alter table pushtoken_android_62 add index idx_uid_pid(uid,pid);