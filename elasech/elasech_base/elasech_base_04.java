> 建立索引
curl -XPUT http://localhost:9200/tree
// 通过映射指定文档类型book的数据结构
{
	"mapping": {
	"book": {
		"properties": {
			"title": {
				"type": "string"
			},
			"author": {
				"type": "object",
				"properties": {
					"name": {
						"type": "object",
						"properties": {
							"firstName": { "type": "string" },
							"lastName": { "type": "string" }
						}
					},
					"age": { "type": "integer" }
				}
			},
			"publish": {
				"type": "object",
				"properties": {
					"year": { "type": "integer" },
					"isbn": { "type": "string" }
				}
			}
		}
	}
	}
}

> 插入文档
curl -XPOST http://localhost:9200/tree/book
{
	"title": "es权威指南",
	"author": {
		"name": {
			"firstName": "Mark",
			"lastName": "Rego"
		},
		"age": 20
	},
	"publish": {
		"year": 2015,
		"isbn": "xx-xx-01"
	}
}

> 查询
curl -XPOST http://localhost:9200/tree/book/_search
{
	"query": {
		author.name.firstName 用match可以查到，用term查不到
		author.age 用term和match都可以查到
		"match": { "author.name.firstName": "Mark" }
	}
}
match会对词条分析，查询"mark"也可以查到文档
term不会分析必须完全匹配，不支持嵌套