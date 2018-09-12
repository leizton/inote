// 减少String.format()的使用
// format包括字符串解析、类型判断, 性能差
for i : [0, 10000000)
	s1 = String.format("%s%d", "abcdefghi_", i);  // 9857 ms
	s1 = "abcdefghi_" + i;                        // 602 ms
	s1 = String.format("%f", new Float(i));       // 10457 ms
	s1 = Float.toString(i);                       // 932 ms

// 取余 vs if
int n = 10, d = 0;
for i : [0, 100000000)
	if (++d == n) d = 0; // 68  ms
	d = (d + 1) % n;     // 762 ms
	d = (d + 1) % 10;    // 389 ms

// Integer.toString vs Float.toString
a:float = 0.123456;
for i : [0, 10000000)
	s = Float.toString(a); // 1472 ms
	s = new Integer((int) (a * 1000000)).toString(); // 483 ms