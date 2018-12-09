# 记录程序执行的trace，可以减少运维工作量

#
RuntimeRecord
  level -> flow -> step -> status -> msg -> info -> where
  level: debug/info/warning/error
  msg:   存放关键变量的值
  where: __file__:__line__:__func__, 当step信息足够时可以省略, 以减少通过异常生成函数栈的开销