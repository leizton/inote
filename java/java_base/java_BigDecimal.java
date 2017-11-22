/*
 * 尽量用String而不是double类型来初始化BigDecimal
 */
System.out.println(new BigDecimal(1.22));    // 1.219999...
System.out.println(new BigDecimal("1.22"));  // 1.22

/*
 * BigDecimal和String类似, 变量是不可变的量
 * 要接收String::trim()/replace()等的返回值
 * 要接收BigDecimal::add()等的返回值
 */ 
BigDecimal a = new BigDecimal("1.22");
a.add(new BigDecimal("1.22"));  // a的值没有改变
System.out.println(a);  // 1.22
System.out.println(a.add(new BigDecimal("1.22")));  // 2.44

/*
 * add(BigDecimal x)的实现
 */
add(BigDecimal x):BigDecimal {
	// 对于2.01, intCompact是201, scale是2
	if this.intCompact != Long.MIN_VALUE
		if x.intCompact != Long.MIN_VALUE
			return BigDecimal.add_1(this.intCompact, this.scale, x.intCompact, x.scale)
		else
			return BigDecimal.add_2(this.intCompact, this.scale, x.intVal, x.scale)
	else
		if x.intCompact != Long.MIN_VALUE
			return BigDecimal.add_2(x.intCompact, x.scale, this.intVal, x.scale)
		else
			return BigDecimal.add_3(this.intVal, this.scale, x.intVal, x.scale)
}
add_1(long xs, int scale1, long ys, int scale2):BigDecimal {
	long n = (long) scale1 - scale2
	if n < 0
		xs *= pow(10, -n)
	else if n > 0
		ys *= pow(10, n)
	return BigDecimal.valueOf(xs + ys, max(scale1, scale2))
}