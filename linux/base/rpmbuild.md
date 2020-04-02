# 创建rpmbuild目录
```cpp
$ sudo yum -y install rpmdevtools
$ rpmdev-setuptree  // 在home目录创建rpmbuild文件
```


# 目录结构
```cpp
/home/user/rpmbuild
SOURCES
  wit-0.0.1
    include/wit/wit.h
    libwit.so
    libwit.a
    main.c             // 简单的helloworld代码
    Makefile
  wit-0.0.1.tar.gz     // tar -czf wit-0.0.1.tar.gz wit-0.0.1
SPECS
  wit.spec
```


# SOURCES/wit-0.0.1/Makefile
```cpp
hello: main.c
  gcc $^ -o $@

install:
  -mkdir -p $(RPM_INSTALL_ROOT)/usr/local/lib64/
  install -m 755 libwit.so $(RPM_INSTALL_ROOT)/usr/local/lib64/
  install -m 644 libwit.a  $(RPM_INSTALL_ROOT)/usr/local/lib64/
  -mkdir -p $(RPM_INSTALL_ROOT)/usr/local/include/wit
  install -m 644 include/wit/wit.h $(RPM_INSTALL_ROOT)/usr/local/include/wit
```


# wit.spec
```c
// 第一行加
%global _missing_build_ids_terminate_build 0

// 填 Makefile install 的文件
%files
%defattr(-,root,root,-)
/usr/local/lib64/libwit.so
/usr/local/lib64/libwit.a
/usr/local/include/wit/wit.h

// 文件末尾加
%define __debug_install_post\
  %{_rpmconfigdir}/find-debuginfo.sh %{?_find_debuginfo_opts} "%{_builddir}/%{?buildsubdir}"\
%{nil}
```


# ref
https://www.linuxidc.com/Linux/2016-12/138080.htm