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


# sort
```py
kv_pairs = my_dict.items()
kv_pairs.sort()
kv_pairs.sort(key=lambda e:e[1], reverse=True)  # 按val排序
kv_pairs.sort(cmp=lambda x,y:my_cmp(x,y))
```


# json
import json
obj = json.loads(jsonstr)
jsonstr = json.dumps(obj)


# read file
f = open('dat', 'r')
for l in f:
  print(l)
for l in f.readlines():
  print(l)
with open('dat', 'r') as f:

# write file
f = open('test.txt', 'w')
f.writelines(['l1'])
f.write('\n')
f.writelines(['l2'])
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


# 遍历目录
```py
def getAllFiles(dirpath):
  total_dirs = []
  total_files = []
  for root, dirs, files in os.walk(dirpath):
    for e in dirs:
      assert(os.path.isdir(os.path.join(root, e)))
      total_dirs += [os.path.join(root, e)]
    for e in files:
      assert(os.path.isfile(os.path.join(root, e)))
      total_files += [os.path.join(root, e)]
  return total_dirs, total_files
```


# class
http://yangcongchufang.com/%E9%AB%98%E7%BA%A7python%E7%BC%96%E7%A8%8B%E5%9F%BA%E7%A1%80/python-object-class.html