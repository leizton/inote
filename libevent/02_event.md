# api
```js
// @evutil_socket_t    int
// @events             类似pollfd.events
// @event_callback_fn  函数指针, (evutil_socket_t fd, short revents, void* arg):void
// @return             struct event类似struct pollfd
event* event_new(event_base* base, evutil_socket_t fd, short events,
                 event_callback_fn cb, void* arg);

// 把event添加到event_base里等待触发(pending)
// @timeout  在event_base里pending的超时时间, NULL时无限长
int event_add(event* ev, timeval* timeout);

int event_del(event* ev);

void event_free(event*);
```

# short events的取值
EV_READ WRITE TIMEOUT PERSIST ET SIGNAL
EV_PERSIST: 设置事件是持久的, 调用回调时不会从`event_base`里移除, 回调函数中无需再调用`event_add()`

# `event_self_cbarg()`
## 使用场景
有时想把event*本身当作回调函数的参数传给回调函数
然而event_new()创建时还没有event*, 这时就用到了`event_self_cbarg()`
`event_self_cbarg()`返回一个magic指针(可能是NULL)
## example
```js
event* ev = event_new(ev_base, fd, EV_TIMEOUT, cb, event_self_cbarg());
event_add(ev, &one_sec);
```

# 事件(event)的生命周期
