# api
```js
// @arg  即evconnlistener_new()时传入的arg, 也是ev_listener->user_data
typedef void (*evconnlistener_cb)(evconnlistener* listener, evutil_socket_t,
    sockaddr* addr, int addr_len, void* arg);

evconnlistener* evconnlistener_new(event_base*, evconnlistener_cb accept_cb, void* arg,
    unsigned flags, int backlog, evutil_socket_t fd);

evconnlistener* evconnlistener_new_bind(event_base*, evconnlistener_cb accept_cb, void* arg,
    unsigned flags, int backlog,  sockaddr* addr, int addr_len);

void evconnlistener_free(evconnlistener*);

// error回调
typedef void (*evconnlistener_errorcb)(evconnlistener*, void* arg);
void evconnlistener_set_error_cb(evconnlistener*, evconnlistener_errorcb accept_error_cb);
```

# evconnlistener的flags
- `LEV_OPT_REUSEABLE`
- `LEV_OPT_THREADSAFE`       设置后执行accept_cb()会加锁以保证线程安全
- `LEV_OPT_DEFERRED_ACCEPT`  通知内核不要立即accept client, 直到有数据可读时才accept
- `LEV_OPT_DISABLED`         创建时不启动listen, 由后续代码手动调用evconnlistener_enable()来启动
- `LEV_OPT_LEAVE_SOCKETS_BLOCKING` 默认情况下accpet的新socket会被设成nonblocking, 设置该选项后不设成nonblocking
- `LEV_OPT_CLOSE_ON_EXEC`
- `LEV_OPT_CLOSE_ON_FREE`

# example
```js
void acceptCb(evconnlistener* listener, evutil_socket_t fd, sockaddr* addr, int addr_len, void* arg) {
    event_base* base = evconnlistener_get_base(listener);
    bufferevent* bufev = bufferevent_socket_new(base, fd, BEV_OPT_CLOSE_ON_FREE);
    bufferevent_setcb(bufev, readCb, writeCb, eventCb, nullptr);
    bufferevent_enable(bufev, EV_READ|EV_WRITE);
}

void acceptErrorCb(evconnlistener* listener, void* arg) {
    int err = EVUTIL_SOCKET_ERROR();
    fprintf(stderr, "get an error %d(%s) on listen", err, evutil_socket_error_to_string(err));
    // event_base_loopexit(evconnlistener_get_base(listener), nullptr);
}
```