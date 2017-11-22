BloomFilter
	判断元素是否存在于集合中
	if   判断结果是true, 表示元素可能存在也可能不存在于集合中, 因为可能发生Hash冲突
	else 判断结果是false, 可以明确元素不存在于集合中
	所以BloomFilter是明确不存在, 不明确存在
	BigTable-用BloomFilter避免在硬盘中寻找不存在的条目
	BloomFilter-也可用于SQL查询的优化