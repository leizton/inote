# bufferevent's callbacks and watermarks
- 每个bufferevent有2个回调: 读回调和写回调
  当从底层的传输层(underlying transport, 如socket)中读数据到输入缓冲(input buffer)时, 触发读回调
  当数据从输出缓冲(output buffer)写到传输层时, 触发写回调
- 每个bufferevent有4个水位线
  1. 读低水位  从传输层读到输入缓冲的数据量大于低水位时, 执行读回调. 默认值0
  2. 读高水位  输入缓冲的数据量大于高水位时, 不再从传输层读数据. 默认值无限大
  3. 写低水位  写传输层后输出缓冲的量小于低水位时, 执行写回调. 默认0, 即直到输出缓冲清空才回调
  4. 写高水位  当bufferevent变成另一个bufferevent的underlying transport时才用到

# create socket-based bufferevent
```js
// @fd  必须是non-blocking, 设置非阻塞的辅助函数evutil_make_socket_nonblocking()
//      fd也可以传-1, 交给后续代码设置
bufferevent* bufferevent_socket_new(event_base* ev_base, evutil_socket_t fd, enum bufferevent_options opts);
```

# 创建bufferevent时可设置的options
- `BEV_OPT_CLOSE_ON_FREE`
  free时关闭底层的传输层
- `BEV_OPT_DEFER_CALLBACKS`
  设置后, 回调函数不会立即执行, 而是放到loop中排队执行
  可避免在bufferevent间存在复杂的依赖时有调用栈溢出的风险
- `BEV_OPT_THREADSAFE`
- `BEV_OPT_UNLOCK_CALLBACKS`
  bufferevent默认是回调函数加锁来保证线程安全, 设置后不加锁

# set/get回调
```js
void bufferevent_setcb(bufferevent*, bufferevent_data_cb read_cb, bufferevent_data_cb write_cb,
                       bufferevent_event_cb event_cb, void* arg);

void bufferevent_getcb(bufferevent*, bufferevent_data_cb*, bufferevent_data_cb*, bufferevent_event_cb*, void**);
```

# connect
```js
// 如果bufev的fd在创建时未设置(参数传-1), 该函数会open一个新的socket
int bufferevent_socket_connect(bufferevent* bufev, sockaddr* addr, int addrlen);

// @family  取值AF_INET,AF_INET6
int bufferevent_socket_connect_hostname(bufferevent*, evdns_base*, int family, char* hostname, int port);

int bufferevent_socket_get_dns_error(bufferevent*);
```

# 手动打开或关闭读写
```js
// @events  取值EV_READ,EV_WRITE,EV_READ|EV_WRITE
void bufferevent_enable(bufferevent* bufev, short events);

void bufferevent_disable(bufferevent* bufev, short events);

short bufferevent_get_enabled(bufferevent *bufev);
```