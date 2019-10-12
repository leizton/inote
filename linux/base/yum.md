# cmds
`yum list $pkg_name`
`yum repolist` 列出当前源
`sudo rm -rf /var/tmp/*` 清理yum包信息缓存


# 添加源
```sh
# yum源的配置文件是xxx.repo, 放在 /etc/yum.repos.d/ 目录下
sudo cp ~/xxx.repo /etc/yum.repos.d/
```

# 只下载不安装
sudo yum install --downloadonly --downloaddir=. $pkgName

# 解压 rpm 包
rpm2cpio xxx.rpm | cpio -id
