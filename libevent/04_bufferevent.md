# bufferevent's callbacks and watermarks
- 每个bufferevent有2个回调: 读回调和写回调
  当从底层的传输层(underlying transport)中读数据到输入缓冲(input buffer)时, 触发读回调
  当数据从输出缓冲(output buffer)写到传输层时, 触发写回调
- 每个bufferevent有4个水位线
  1. 读低水位. 从传输层读到输入缓冲的数据量大于低水位时, 调用读回调. 默认值0
  2. 读高水位. 输入缓冲的数据量大于高水位时, 不再从传输层读数据. 默认值无限大
  3. 写低水位. 写传输层后输出缓冲的量小于低水位时, 调用写回调. 默认0, 即直到输出缓冲清空才回调
  4. 写高水位. 当bufferevent变成另一个bufferevent的underlying transport时才用到