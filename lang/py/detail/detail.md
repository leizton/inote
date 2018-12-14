# 获取本机ip
```py
import socket
[(s.connect(('8.8.8.8', 53)), s.getsockname()[0], s.close()) for s in [socket.socket(socket.AF_INET, socket.SOCK_DGRAM)]][0][1]
```


# json
import json
obj = json.loads(jsonstr)
jsonstr = json.dumps(obj)