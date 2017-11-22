"""
./scipy/stats/stats.py

计算2个随机序列的Kolmogorov-Smirnov值

例子:
>>> np.random.seed(12345678); n1 = 200; n2 = 300
>>> data1 = scipy.stats.norm.rvs(size=n1, loc=0., scale=1)
>>> data2 = scipy.stats.norm.rvs(size=n2, loc=0.5, scale=1.5)
>>> scipy.stats.ks_2samp(data1, data2)
(0.20833333333333337, 4.6674975515806989e-005)
>>> data2 = stats.norm.rvs(size=n2, loc=0., scale=1.0)
>>> scipy.stats.ks_2samp(data1, data2)
(0.07999999999999996, 0.41126949729859719)

返回的第1个数越小、第2个数越大，则随机序列data1和data2的概率分布越相似
"""
def ks_2samp(data1, data2):
	data1 = np.sort(data1)  # 从小到大排序
	data2 = np.sort(data2)
	n1 = data1.shape[0]
	n2 = data2.shape[0]
	data_all = np.concatenate([data1, data2])  # 2个数组连接成一个更大的数组

	# searchsorted(d, da, 'right') 把da中每个元素插入到d中，相等时放在右边，返回每个元素插入后在d中的索引
	cdf1 = np.searchsorted(data1, data_all, side='right') / (1.0*n1)
	cdf2 = np.searchsorted(data2, data_all, side='right') / (1.0*n2)
	d = np.max(np.absolute(cdf1 - cdf2))  # 最大差值
	en = np.sqrt(n1 * n2 / float(n1 + n2))
	try:
		prob = distributions.kstwobign.sf((en + 0.12 + 0.11 / en) * d)
	except:
		prob = 1.0

	# Ks_2sampResult = namedtuple('Ks_2sampResult', ('statistic', 'pvalue'))
	# namedtuple用来产生可以使用名称来访问元素的数据对象
	return Ks_2sampResult(d, prob)


distributions.kstwobign.sf()的实现:
	distributions.py
		from ._continuous_distns import *
	_continuous_distns.py
		import scipy.special as sc  # 用c语言实现的函数
		class kstwobign_gen(rv_continuous):
			def _cdf(self, x):
				return 1.0 - sc.kolmogorov(x)
			def _sf(self, x):
				return sc.kolmogorov(x)  # distributions.kstwobign.sf()的实际位置
			def _ppf(self, q):
				return sc.kolmogi(1.0 - q)
		kstwobign = kstwobign_gen(a=0.0, name='kstwobign')


./scipy/special/cephes/kolmogorov.c
double kolmogorov(double y) {
	double p, t, r, sign, x;
	if (y < 1.1e-16) return 1.0;
	x = -2.0 * y * y;
	sign = 1.0;
	p = 0.0;
	r = 1.0;
	do {
		t = exp(x * r * r);  // <math.h>
		p += sign * t;
		if (t == 0.0)
			break;
		r += 1.0;
		sign = -sign;
	} while ((t / p) > 1.1e-16);
	return (p + p);
}