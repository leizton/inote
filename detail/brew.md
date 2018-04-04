# 安装thrift 0.9.3
```sh
git clone https://github.com/Homebrew/homebrew-core
find . -name thrift.rb  # ./Formula/thrift.rb
git log --pretty=online ./Formula/thrift.rb  # 找到0.9.3的checkout出来
brew install ./Formula/thrift.rb
    # thrift.rb:25:in `block in <class:Thrift>'
    # 把25行的reversion 2改成rebuild 2
thrift -version  # check
```