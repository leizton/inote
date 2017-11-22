Hashing
	在"com.google.common.hash"包下.
内部类
	Md5Holder
		static final HashFunction MD5 = new MessageDigestHashFunction("MD5", "Hashing.md5()")
	Crc32Holder
		static final HashFunction CRC_32 = checksumHashFunction(ChecksumType.CRC_32, "Hashing.crc32()")
静态方法
	/* string md5_NoKey = Hashing.md5().hashString(message, Charsets.UTF_8).toString();
	 * string md5_WithKey = Hashing.md5().newHasher()
	 *         .putString(key, Charsets.UTF_8)
	 *         .putString(message, Charsets.UTF_8)
	 *         .hash().toString();
	 * 实际上就是拼接key和message后新字符串的MD5.
	 * md5_WithKey 等于 Hashing.md5().hashString(key + message, Charsets.UTF_8).toString()
	 */
	md5():HashFunction { return Md5Holder.MD5 }
	// String crc32 = Hashing.crc32().hashString(message, Charsets.UTF_8).toString();
	crc32():HashFunction { return Crc32Holder.CRC_32 }


MD5即Message-Digest Algorithm 5, 信息-摘要算法5.
用于一致性验证. 对一段信息(Message)产生信息摘要(Message-Digest)，以防止被篡改
算法特点:
  1. 压缩性: 任意长度的数据, 算出的MD5值长度都是固定的
  2. 容易计算: 从原数据计算出MD5值很容易
  3. 抗修改性: 对原数据进行任何改动, 即使只修改1个字节, 所得到的MD5值都有很大区别
  4. 抗碰撞性强: 已知原数据和其MD5值, 想找到一个具有相同MD5值的数据(即伪造数据)是非常困难的