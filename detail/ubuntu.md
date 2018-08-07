# 阿里源
$ sudo vi /etc/apt/sources.list
deb-src http://archive.ubuntu.com/ubuntu xenial main restricted #Added by software-properties
deb http://mirrors.aliyun.com/ubuntu/ xenial main restricted
deb-src http://mirrors.aliyun.com/ubuntu/ xenial main restricted multiverse universe #Added by software-properties
deb http://mirrors.aliyun.com/ubuntu/ xenial-updates main restricted
deb-src http://mirrors.aliyun.com/ubuntu/ xenial-updates main restricted multiverse universe #Added by software-properties
deb http://mirrors.aliyun.com/ubuntu/ xenial universe
deb http://mirrors.aliyun.com/ubuntu/ xenial-updates universe
deb http://mirrors.aliyun.com/ubuntu/ xenial multiverse
deb http://mirrors.aliyun.com/ubuntu/ xenial-updates multiverse
deb http://mirrors.aliyun.com/ubuntu/ xenial-backports main restricted universe multiverse
deb-src http://mirrors.aliyun.com/ubuntu/ xenial-backports main restricted universe multiverse #Added by software-properties
deb http://archive.canonical.com/ubuntu xenial partner
deb-src http://archive.canonical.com/ubuntu xenial partner
deb http://mirrors.aliyun.com/ubuntu/ xenial-security main restricted
deb-src http://mirrors.aliyun.com/ubuntu/ xenial-security main restricted multiverse universe #Added by software-properties
deb http://mirrors.aliyun.com/ubuntu/ xenial-security universe
deb http://mirrors.aliyun.com/ubuntu/ xenial-security multiverse
$ sudo apt update

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

# gcc降级
apt-get install gcc-4.9
apt-get install g++-4.9
update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.9 20
update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-5 10    # gcc-5分配更低的优先级
update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-4.9 20
update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-5 10
update-alternatives --config gcc  # 选择版本
update-alternatives --config g++