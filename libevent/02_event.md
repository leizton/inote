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

void event_free(event* ev);

// @events  待检查的事件类型
// @tv_out  非NULL时, 返回调用event_add()时设置的超时时间
// @return  events中ev正在pending的事件类型
int event_pending(event* ev, short events, timeval* tv_out);
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

# `event_pair 和 event_assign()`
event_assign()用于已经有了event结构体的实例时, 初始化这个实例
```js
struct event_pair {
    evutil_socket_t fd;
    event read_event;
    event write_event;
};
event_pair* ev_pair = malloc(sizeof(event_pair));
ev_pair->fd = fd;
event_assign(&ev_pair->read_event, ev_base, fd, EV_READ|EV_PERSIST, read_cb, ev_pair);
event_assign(&ev_pair->write_event, ev_base, fd, EV_WRITE|EV_PERSIST, write_cb, ev_pair);
```

# `event_get_assignment()`
获取event实例的字段值
```js
// 传NULL参数值表示不想获取这个字段的值
void event_get_assignment(event*, event_base**,
        evutil_socket_t* fd_out, short* events_out, event_callback_fn* cb_out, void** arg_out);
```

# `event_base_once()`
创建并add只调度一次的事件
形参表和`event_new()`类似, 只是增加了`event_add()`用到的timeout
```js
int event_base_once(event_base*, evutil_socket_t, short, event_callback_fn, void*, timeval*);
```

# `event_active()`
```js
// 手动激活事件
// @revents  本次需要激活的事件集
// @ncalls   可忽略的参数, 传0即可
void event_active(event *ev, int revents, short ncalls);
```

# timeout类型事件的辅助接口
```js
#define evtimer_new(base, cb, arg)  event_new(base, -1, 0, (cb), (arg))
#define evtimer_add(ev, tv)         event_add((ev), (tv))
#define evtimer_del(ev)             event_del((ev))
#define evtimer_pending(ev, tv_out) event_pending((ev), EV_TIMEOUT, (tv_out))
```

# 优化timeout


# 事件(event)的生命周期
