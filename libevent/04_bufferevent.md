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
typedef void (*bufferevent_data_cb)(bufferevent* bufev, void* arg);
typedef void (*bufferevent_event_cb)(bufferevent* bufev, short revents, void* arg);

void bufferevent_setcb(bufferevent* bufev, bufferevent_data_cb read_cb, bufferevent_data_cb write_cb,
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

# api
```js
// 设置水位线
// high_mark设成0表示unlimited
void bufferevent_setwatermark(bufferevent* bufev, short events, size_t low_mark, size_t high_mark);

int bufferevent_setfd(bufferevent*, evutil_socket_t);
evutil_socket_t bufferevent_getfd(bufferevent*);

void bufferevent_lock(bufferevent*);
void bufferevent_unlock(bufferevent*);

evbuffer* bufferevent_get_input(bufferevent*);
evbuffer* bufferevent_get_output(bufferevent*);

// 写[buf, buf+size)上的数据
// @return  写成功返回0, 否则返回-1
int bufferevent_write(bufferevent*, char* data, size_t size);
// @return  实际读到的字节数
int bufferevent_read(bufferevent*, char* data, size_t size);

// @return 0,success; -1,error
int bufferevent_write(bufferevent*, evbuffer*);
// @return 0,success; -1,error
int bufferevent_read(bufferevent*, evbuffer*);

// 设置读写超时时间, 在这段时间里如果bufev上没有成功地读写数据,
// 则回调event_cb, revents是BEV_EVENT_TIMEOUT|BEV_EVENT_READING, 或BEV_EVENT_TIMEOUT|BEV_EVENT_WRITING
void bufferevent_set_timeouts(bufferevent*, timeval* read_timeout, timeval* write_timeout);

// 刷新读写, 尽可能读或写最多的数据到underlying transport
// @io_type     取值EV_READ,EV_WRITE,EV_READ|EV_WRITE
// @flush_mode  取值BEV_FINISHED表示没有更多数据写了
int bufferevent_flush(bufferevent*, short io_type, enum bufferevent_flush_mode flush_mode);
```

# bufferevent 过滤器
```js
enum bufferevent_filter_result { BEV_OK, BEV_NEED_MORE, BEV_ERROR };

// @dst_limit  写到dst的数据量的上限, -1表示无限
// @ctx        调用bufferevent_filter_new()时传入的参数
// @return     BEV_OK,成功写数据到dst
typedef bufferevent_filter_result (*bufferevent_filter_cb)(
    evbuffer* src, evbuffer* dst, ev_ssize_t dst_limit, bufferevent_flush_mode mode, void* ctx);

// 在一个bufferevent的基础上创建一个带过滤器的bufferevent
bufferevent* bufferevent_filter_new(bufferevent* underlying,
    bufferevent_filter_cb input_filter, bufferevent_filter_cb output_filter,
    int opts, void (*free_context)(void* ctx), void* ctx);
```

# rate limit
令牌桶算法
```js
// @read_rate   每个tick放入read_rate个令牌, 每个令牌对应一个byte
//              example: 当tick=100ms, read_rate=300, 读限流3000B/s
// @read_burst  read_rate是平均最大速率, read_burst是实际最大速率
ev_token_bucket_cfg* ev_token_bucket_cfg_new(size_t read_rate, size_t read_burst,
    size_t write_rate, size_t write_burst, timeval* tick);

int bufferevent_set_rate_limit(bufferevent*, ev_token_bucket_cfg*);

void ev_token_bucket_cfg_free(ev_token_bucket_cfg*);
```