Cookie例子
	// Cookie: username
	String username = "张三xyz";
	Cookie usernameCookie = new Cookie("username", URLEncoder.encode(username, "utf-8"));
	usernameCookie.setPath("/");
	usernameCookie.setMaxAge((int)TimeUnit.DAYS.toSeconds(1));  // 1天的秒数, java.util.concurrent.TimeUnit
	response.addCookie(usernameCookie);
	// Cookie: age
	Cookie ageCookie = new Cookie("age", "20");
	addCookie.setDomain("www.baidu.com");
	response.addCookie(addCookie);
	// 结果
	响应报文头部:
		Set-Cookie:name=%E5%BC%A0%E4%B8%89xyz;Max-Age=86400;path=/  // E5BCA0E4B889是"张三"的utf-8编码出的bytes
		Set-Cookie:age=20;domain=www.baidu.com
	浏览器发现domain和url里的domain不同时, 不会存下该cookie, 所以ageCookie不会种在浏览器上.