tomcat7的默认字符集是iso-8859-1,
所以request.getParameter("name")返回的String是从new String(bytes, "iso-8859-1")而来.
name是用iso-8859-1字符集对utf-8字节编码.

当URL是"/login.do?name=%E5%BC%A0%E4%B8%89"("/login.do?name=张三"),
getParameter("name")是new String(new byte[]{0xE5, BC A0 E4 B8 89}, "iso-8859-1").
所以需要String name = new String(request.getParameter("name").getBytes("iso-8859-1"), "utf-8"),
从utf16转出iso-8859-1编码的字节, 再从iso-8859-1编码的字节按utf-8编码得到utf16才能得到正确的name.

String默认的getBytes()使用utf-8, 即name.getBytes()等同于name.getBytes("utf-8").

为了让tomcat7不需要每次都从iso-8859-1转到utf-8,
应该在 ${TOMCAT_HOME}/conf/server.xml 的 <Connector>标签 里增加属性 URIEncoding="utf-8".
注意是URI, 而不是URL.
这样request.getParameter("name")的String是new String(bytes, "utf-8").

byte[] bs = new byte[]{0xE5, BC A0 E4 B8 89};
utfStr = new String(bs, "utf-8");
isoStr = new String(bs, "iso-8859-1");
把utfStr和isoStr的value(char[])打印成short, 如下:
	for (char c : s.toCharArray())
		System.out.print(String.format("%d ", (short)c));
结果:
utfStr: 24352 19977 (2个utf16字符)
isoStr: 229 188 160 228 184 137 (6个utf16字符)

PS:
tomcat9已经把默认字符集设成"utf-8".