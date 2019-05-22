# GOROOT  GOPATH  GOBIN
GOROOT   golang的安装目录
GOPATH    自己的工程，以及第三方库 的存放目录
GOBIN        go install xxx  的安装目录

例如
export GOROOT="$HOME/bin/go1.12.3"
export GOPATH="$HOME/gopath"
export GOBIN="$GOPATH/bin"
export PATH="$GOBIN:$GOROOT/bin:$PATH"


# go test
-v 输出到console
-count=1 关闭cached
