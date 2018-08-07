import sys
import time as tm

def help():
  print('timest               # current time')
  print('timest 946656000     # time-num of seconds')
  print('timest 946656000000  # time-num of milliseconds')
  print('timest 946656000.0   # float time-num with millisecond')
  print('2000-01-01 00:00:00  # time-str')

def now():
  return tm.localtime(tm.time())

def numstr2Tm(str):
  idx = str.find('.')
  if idx < 0:
    ts = long(str)
    if ts < 2**32:
      return ts, 0
    else:
      return ts / 1000, ts % 1000
  else:
    return long(str[0:idx]), long(str[(idx+1):len(str)])

def tm2Str(time):
  return tm.strftime('%Y-%m-%d %H:%M:%S', time)

def str2Tm(str):
  return tm.strptime(str, '%Y-%m-%d %H:%M:%S')

def display(time):
  ts = tm.mktime(time)
  tsec,tms = numstr2Tm(str(ts))
  print(tsec)
  print('%d%03d' % (tsec, tms))
  print(ts)
  print(tm2Str(time))

def displayTs(tsec, tms):
  print("%d.%03d" % (tsec, tms))
  tsecStr = tm2Str(tm.localtime(float(tsec)))
  print("%s.%03d" % (tsecStr, tms))

if __name__ == '__main__':
  argc = len(sys.argv)
  if argc == 2 and sys.argv[1] == '-h':
    help()
  elif argc == 1:
    display(now())
  elif argc == 2:
    tsec,tms = numstr2Tm(sys.argv[1])
    displayTs(tsec, tms)
  elif argc == 3:
    param = sys.argv[1] + ' ' + sys.argv[2]
    display(str2Tm(param))
