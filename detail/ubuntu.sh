# ubuntu连上wifi后不能上网
$ sudo vi /etc/resolv.conf  #加新行: nameserver 127.0.1.1
$ sudo service network-manager restart

# 修改网卡的mtu值
$ sudo ifconfig eno1(网卡名) mtu 1400

# 移除过期的源
$ sudo apt-get update | grep Failed
  # 查找出过期的源
  # 例如: http://ppa.launchpad.net/fcitx-team/nightly/ubuntu artful Release
$ sudo add-apt-repository --remove ppa:fcitx-team/nightly