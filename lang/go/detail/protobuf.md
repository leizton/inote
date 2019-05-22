# 安装
```sh
go get -d -u github.com/golang/protobuf/protoc-gen-go
git -C "$(go env GOPATH)"/src/github.com/golang/protobuf checkout "v1.3.1"
  # 指定某个版本
go install github.com/golang/protobuf/protoc-gen-go
  # 在 $GOBIN 里生成 可执行文件 protoc-gen-go
```

# 编译
protoc --go_out=. test.proto

# example
x := &Foo{}
err := proto.Unmarshal(bs, x)