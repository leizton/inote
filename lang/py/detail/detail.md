# 编码
```py
#!/usr/bin/env python
#encoding=utf-8
```


# 获取本机ip
```py
import socket
[(s.connect(('8.8.8.8', 53)), s.getsockname()[0], s.close()) for s in [socket.socket(socket.AF_INET, socket.SOCK_DGRAM)]][0][1]
```


# string
```py
# ','.join(lst)
  lst的元素必须都是str类型，否则用','.join(str(e) for e in lst)
# 字符串对齐
print '%s %s' % (s1.ljust(20), s2)  # s1左对齐, 不足20时补空格
```


# dict
```py
# 遍历
for k in my_dict
for k in my_dict.iterkeys()
for k in my_dict.keys()
for v in my_dict.itervalues()
for v in my_dict.values()
for k,v in my_dict.iteritems()
for k,v in my_dict.items()
# 删除
del my_dict[k1]
```


# json
import json
obj = json.loads(jsonstr)
jsonstr = json.dumps(obj)


# read/write file
f = open('dat', 'r')
for l in f:
  print(l)
for l in f.readlines():
  print(l)
with open('dat', 'r') as f:
--
f = open('test.txt', 'w')
f.writelines(line)
f.write('\n')


# 打印当前函数栈
import traceback
s = ''
for l in traceback.format_stack():
  s += '\n  > ' + l.strip().replace('\n', '')


# 路径
相对路径转绝对路径
  os.path.abspath(relPath)
绝对路径转相对路径
  os.path.relpath(absPath)
获取上层目录路径
  os.path.dirname(path)


# class
http://yangcongchufang.com/%E9%AB%98%E7%BA%A7python%E7%BC%96%E7%A8%8B%E5%9F%BA%E7%A1%80/python-object-class.html