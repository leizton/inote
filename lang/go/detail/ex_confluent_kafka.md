confluent-kafka-go 封装了 librdkafka
依赖动态库: liblz4.so.1 librdkafka.so.1
export LD_LIBRARY_PATH="../libkafka:$LD_LIBRARY_PATH"
`go build` 和 `go test` 需要加 `-a` 选项