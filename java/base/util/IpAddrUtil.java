private String getNetworkIp() throws Exception {
  Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
  while (nis.hasMoreElements()) {
      NetworkInterface ni = nis.nextElement();
      Enumeration<InetAddress> inets = ni.getInetAddresses();
      while (inets.hasMoreElements()) {
        InetAddress inet = inets.nextElement();
        if (inet instanceof Inet4Address && !inet.isLoopbackAddress()) {
          // 返回非回环的ipv4地址
          return inet.getHostAddress();
        }
     }
  }
  throw new Exception("获取本机ip地址失败.");
}