TF/IDF-评分算法

> 执行java脚本
打包 mvn clean package -pl elasticscript
cp elasticscript/target/elasticscript-1.0.jar /home/whiker/bin/elasticsearch-5.0.1/plugins/elasticscript
touch plugins/elasticscript/plugin-descriptor.properties
curl -XPOST http://localhost:9200/test/users/_search
{
	"query": {
		"match_all": {}
	},
	"sort": {
		"_script": {
			"script": "multi_fields_sort",
			"params": {
				"fields": "age,name"
			},
			"type": "string",
			"lang": "native"
		}
	}
}