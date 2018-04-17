import sys
import httplib


def upload(host, port, srcfile):
  body = open(srcfile, "rb").read()
  conn = httplib.HTTPConnection(host, port)
  conn.request("PUT", "/" + srcfile[srcfile.find('/')+1:], body)
  resp = conn.getresponse()
  print resp.status, resp.reason


# python upload.py data/user.dat
if __name__ == '__main__':
  if len(sys.argv) != 2:
    print('invalid argv')
    exit(-1)
  srcfile = sys.argv[1]
  upload("127.0.0.1", 10000, srcfile)
