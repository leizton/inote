// 安装和运行
以守护进程方式运行  $ ./bin/elasticsearch -d
curl http://localhost:9200/?pretty
查看集群状态  curl http://localhost:9200/_cluster/health?pretty
关掉整个集群  curl http://localhost:9200/_cluster/nodes/_shutdown
关掉某个节点  curl http://localhost:9200/_cluster/nodes/$node_id/_shutdown

// es的监控工具Marvel
$ ./bin/plugin install marvel-agent

// Lucene的基本概念
文档  字段(字段名称，字段内容)  词项(文档中的单词)  词条

// 倒排索引
Map<词项, 计数_文档编号>

// 段合并
检索大段比检索有重复数据的很多小段速度更快

// 分析器
获取文档的倒排索引，把查询字符串转成搜索词项的过程称作文本分析
分析器实现文本分析
分析器由分词tokenizer，过滤filter，字符映射charMapper组成
过滤器: 小写过滤器，同义词过滤器等

// lucene的查询语法
一个查询由词项和操作符组成
操作符有: AND OR NOT +(必须包含) -(不包含)
字段查询
	"title:(+elastics +book)" 或 "title:+elastics title:+book"
	查询同时包含elastics和book的名称是title的字段
词项修饰符
	通配符(? *)  ~(指定编辑距离，如read~2可以匹配reader/road)

// 数据架构
索引(_index)，可以看成关系数据库的一张表
文档，可以看成一行记录，包括多个字段
文档类型(_type)，每个索引可以有多种类型。
  同一索引中同名字段的数据类型必须相同，例如同一索引中所有文档的title字段都是短文本类型
映射，字段到数据类型的映射。虽然没有明确地定义文档的每个字段的数据类型，但elastic是按照类型来分析的

// ElasticSearch的基本概念
节点和集群
分片，是一个独立的lucene索引。当查询需要多个分片时，es会去分片所在的几个节点上查询并合并结果
副本，有主分片和副本分片两种
时光之门，控制集群的状态

// 插入文档的过程
插入新文档时，用户指定文档的索引
  es把文档发到对应的节点上
  这个节点知道目标索引的信息，并根据文档唯一标识确定新文档放在哪个分片上

// 插入文档
必须用"post"方法
http://localhost:9200/test/users
{
	"name":"张三",
	"age": 20,
	"tag": ["young", "active"]
}
返回:
  _index=test, _type=users, _id=文档唯一标识, _version=1,
  _shards=(total=2, successful=1, failed=0),
  created=true

// 查看索引的信息，包括映射(每个字段设置的数据类型)，分片数等
curl -XGET http://localhost:9200/$_index
返回
{
	"test": {  // 索引名
		"mappings": {   // 映射
			"users": {  // 文档类型
				"properties": {
					"age": {"type": "long"},
					"name": {"type": "string"},
					"tag": {"type": "string"}
				}
			}
		},
		...
	}
}

// 查询文档
用"get/post"方法
> 用文档唯一标识(_id)查询
  http://localhost:9200/test/users/$_id?pretty
  返回字段:
  	"found=true/false"，表示是否查到
    "_source"，源数据
> 匹配文档中的字段
  URI格式: /$_index/$_type/_search?pretty&q=字段:值
  例子: http://localhost:9200/test/users/_search?pretty&q=name:张三ANDage=20
       其中，"q=name:张三ANDage=20"是lucene的查询语法
  返回
{
  "took": 5,  // 查询用时(毫秒)
  "timed_out": false,
  "_shards": {  // 查询了几个分片，成功几个，失败几个
    "total": 5, "successful": 5, "failed": 0
  },
  "hits": {
    "total": 1,
    "max_score": 0.03978186,  // 最高得分
    "hits": [  // 命中的文档数组
      {
		// 第1个文档
        "_index": "test",
        "_type": "users",
        "_id": "AVhcDn1orlXgy5u4LPoc",
        "_score": 0.03978186,
        "_source": {
          "name": "张三",
          "age": 20,
          "tag": [ "young", "active" ]
        }
      }
]}}

// 分析
curl -XPOST http://localhost:9200/test/_analyze?field=name -d '张三'
返回
{
  "tokens": [
    {
      "token": "张",
      "start_offset": 0, "end_offset": 1,  // 在数据中的位置
      "type": "<IDEOGRAPHIC>", "position": 0
    },
    {
      "token": "三",
      "start_offset": 1, "end_offset": 2,
      "type": "<IDEOGRAPHIC>", "position": 1
    }
  ]
}

// 更新文档
用"post/put"方法
更新索引中的文档需要先删除旧文档，再插入新文档，因为lucene的倒排索引建立后不可修改
> curl -XPOST http://localhost:9200/$_index/$_type/$doc_id
       -d '{"age":23}'
  整个文档变成{ "age": 23 }，原来的name和tag字段被丢掉

// 删除
必须用"delete"方法
删除索引，curl -XDELETE http://localhost:9200/$_index
删除文档，curl -XDELETE http://localhost:9200/$_index/$_type/$doc_id

// 版本控制
> 插入时指定版本和文档id
  http://localhost:9200/test/users/$doc_id?version=$version&version_type=external
  用post方法，指定"version_type=external"，version必须是long类型
> 删除时指定版本
  curl -XDELETE http://localhost:9200/$_index/$_type/$doc_id?version=$version

// 一次查询多个索引
多个索引间用逗号隔开
http://localhost:9200/$_index_1,$_index_２/$_type/_search