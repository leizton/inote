JSON: JavaScript Object Notation
JSONP: JSON with Padding

json是数据交换格式, jsonp是一种非官方跨域数据交互协议.
很明显的区别是json是数据格式, jsonp是一种协议.

问题: ajax无权限进行跨域请求, 所以不能通过ajax解决跨域访问.
解决方法:
	html的<script> <img>等标签的src属性里定义的url是可以跨域的,
	这样, 客户端可通过src拿到跨域服务器生成的json文件.
具体例子:
<!-- 本地html文件 -->
<html>
<head>
	<script type="text/javascript">
		var localHandler = function(ret) {
			alert("其他域的远程服务器返回的数据: " + ret.data);
		};
		// url的callback参数指定本地回调函数
		var url = "http://其他域/request.js?callback=localHandler";
		var script = document.createElement("script");
		script.setAttribute("src", url);
		// 插入<script src='url'>
		// 远程服务器生成js代码: localHandler({status:0, data:'客户端需要的数据'});
		// 远程服务器把数据嵌入js代码中
		document.getElementByTagName("head")[0].append(script);
	</script>
</head>
...
</html>

通过 CORS 解决跨域更简单
https://segmentfault.com/a/1190000015597029
https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Access_control_CORS
```go
router.Use(CORSMiddleware())

func CORSMiddleware() gin.HandlerFunc {
  return func(ctx *gin.Context) {
    ctx.Writer.Header().Set("Access-Control-Allow-Origin", "*XXX*")
    ctx.Writer.Header().Set("Access-Control-Allow-Credentials", "true")
    ctx.Writer.Header().Set("Access-Control-Allow-Headers",
      "Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, user"+
          "Authorization, accept, origin, Cache-Control, X-Requested-With")
    ctx.Writer.Header().Set("Access-Control-Allow-Methods", "POST, OPTIONS, GET, PUT")
    ctx.Writer.Header().Set("Access-Control-Max-Age", common.AccessControlMaxAge)

    if ctx.Request.Method == "OPTIONS" {  // 提供给浏览器的预检
      ctx.AbortWithStatus(204)
      return
    }
    ctx.Next()
  }
}
```