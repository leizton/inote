# constraint condition
- charset = 'ascii'
- metricName.length < 128
- tagKey/tagValue.length < 64
- tagsNum <= 10

# feature
- memory lru cache
- reversed indexes + bitmap
- sql query
- query: preagg, archive, segment-query
- qps: first derivative of values versus time, zero-fill or constant-interpolation
- metrics manager: uid, quota plan
- aggregate function: avg sum median min max percentile