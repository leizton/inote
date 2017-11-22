// 分片数目和副本数目
副本数目可以在索引创建好后根据集群资源来调整
索引的分片数目在索引创建好后，要想改变只能另建索引
> 创建索引时，指定分片数和副本数:
-XPut http://localhost:9200/$_index
{
	"settings": {
		"number_of_shards": 5,
		"number_of_replicas": 1
	}
}
共有10个物理lucene索引，分片数×(1 + 副本数)

// 配置索引的映射
创建索引时，设置数据类型的确定机制
-XPut http://localhost:9200/$_index
{
	"mapping": {
		"users"(_type): {
			"numeric_detection": true,
			"dynamic_date_formats": ["yyyy-MM-dd hh:mm:ss"]
		}
	}
}
{
	"mapping": {
		"users"(_type): {
			// 禁用类型推测，自定义映射
			// 但仍然可以插入其他类型的值，如name可以插入int类型的值
			"dynamic": "false",
			"properties": {
				"id": {"type": "long", "store":"yes", "precision_step":"0"},
				"name": {"type": "string"},
				"age": {"type": "int"}
			}
		}
	}
}

// es的核心类型
string  number  date  boolean  binary
number包括byte,short,integer,long,float,double