# 获取本机ip
```py
import socket
[(s.connect(('8.8.8.8', 53)), s.getsockname()[0], s.close()) for s in [socket.socket(socket.AF_INET, socket.SOCK_DGRAM)]][0][1]
```


# json
import json
obj = json.loads(jsonstr)
jsonstr = json.dumps(obj)


# 遍历词典
for k in my_dict
for k in my_dict.iterkeys()
for k in my_dict.keys()
for v in my_dict.itervalues()
for v in my_dict.values()
for k,v in my_dict.iteritems()
for k,v in my_dict.items()


# string
## ','.join(lst)
  lst的元素必须都是str类型，否则用','.join(str(e) for e in lst)