/etc 存放配置文件
/var 目录存放系统日志
/usr 全称unix software resources


$ cd -  # 返回最近一次cd前的目录


$ tree dirName  # 打印目录树


$ scp -r $username@$host:$dirPath $localDirPath


$ env  查看环境变量
$ which ls 或 whereis ls


$ echo '\n'  输出\n字符串和换行符(echo默认追加)
$ echo -e '\n'  输出两行, -e用于转义字符


环境变量
$ COLOR=blue  设置变量, =左右不能有空格
$ export COLOR=blue  把变量COLOR加入环境变量, "export $COLOR"没有作用
$ env | grep COLOR
$ vi myenv.sh  写入"export COLOR=blue"  # 从文件导入环境变量
$ source myenv.sh


shell运算符
-eq -ne -lt -gt -le -ge


安装软件
$ sudo apt-get install/remove git
$ sudo yum install/remove q-node


软链接
$ touch a.txt && ln -s a.txt b.txt
$ vi a.txt
	drwxrwxr-x 2 whiker whiker 4096  3月  8 11:01 ./
	drwxrwxr-x 5 whiker whiker 4096  3月  7 19:32 ../
	-rw-rw-r-- 1 whiker whiker    9  3月  8 11:01 a.txt
	lrwxrwxrwx 1 whiker whiker    5  3月  8 11:01 b -> a.txt
(a.txt和b的硬链接数都是1)
$ cat b
	11111111
硬链接
$ ln a.txt c
	drwxrwxr-x 2 whiker whiker 4096  3月  8 11:02 ./
	drwxrwxr-x 5 whiker whiker 4096  3月  7 19:32 ../
	-rw-rw-r-- 2 whiker whiker    9  3月  8 11:01 a.txt
	lrwxrwxrwx 1 whiker whiker    5  3月  8 11:01 b -> a.txt
	-rw-rw-r-- 2 whiker whiker    9  3月  8 11:01 c
	(a.txt和c的硬链接数都是2, b的是1)
$ rm a.txt
	drwxrwxr-x 2 whiker whiker 4096  3月  8 11:02 ./
	drwxrwxr-x 5 whiker whiker 4096  3月  7 19:32 ../
	lrwxrwxrwx 1 whiker whiker    5  3月  8 11:01 b -> a.txt
	-rw-rw-r-- 1 whiker whiker    9  3月  8 11:01 c
	(c的硬链接树变成1, b -> a.txt变成红色表示b指向的a.txt不存在)
$ cat b
	cat: b: 没有那个文件或目录
$ cat c
	11111111


find . -atime n(精确的前第n天) -n(前0~n天) +n(n天前)
	 . -amin 分钟
	-atime(访问) -mtime(修改) -ctime(创建)


stat的3个时间
	最近改动: 对文件的修改, 文件属性改变(权限/大小等), 会引起该时间更新
	最近更改: 对文件的修改, 会引起该时间更新
	最近访问: 对文件的修改, 修改后第1次查看, 会引起该时间更新, 修改后第2次查看不会引起更新


awk语法
awk -F '分割符1|分割符2' 'BEGIN{处理前语句块} {单行处理} END{结束后语句}' 输入文件
awk -F "    |】" '{sendNums[$2]++;}
    END{for(sender in sendNums) printf "%s    %d\n",sender,sendNums[sender];}' $filename > $count_file

awk内置变量
	NR  当前行号
	NF  当前行的列数
	FNR  awk可以一次处理多个文件, 累计当前行号
awk -F ',' '{print NR,NF}' a.txt

awk处理列, grep处理行
$ netstat -t | awk '{print $1, $2, $3}'  输出第1,2,3列
$ netstat -t | awk '{print $1, $2, $3}' OFS='\t'  输出第1,2,3列, 用\t分开每列
$ netstat -t | awk '{printf "%-8s %-8s %-8s\n", $1, $2, $3}'
$ netstat -t | awk '$2==0 && $6=="CLOSE_WAIT"'  过滤
awk正则匹配, ~/.../
$ netstat -t | awk '$6~/^(CLOSE_WAIT|TIME_WAIT)$/ {print NR,$1,$4,$6}' OFS='\t'  NR是行号
$ netstat -t | awk '$6~/^[CT].*$/ {print NR,$1,$4,$6}' OFS='\t'  找出CLOSE_WAIT或TIME_WAIT


sed替换文本
$ cat a.txt
	a 123
	a 123a
$ sed 's/a 123/b 456/g' a.txt  结果输出到终端
	b 456
	b 456a
$ sed -i 's/a 123/b 456/g' a.txt  用-i把结果保存到a.txt
$ sed 's/^/START /g' a.txt  在每行开始加'START '
	START b 456
	START b 456a
$ sed 's/$/ END/g' a.txt  在每行末尾加' END'
	b 456 END
	b 456a END
$ sed '2,2s/$/ END/g' a.txt  只替换从2到2行, 或 sed '2s/$/ END/g' a.txt
	b 456
	b 456a END


nohup
$ nohup ./start.sh >/dev/null 2>&1 &

$ sudo -b ./start.sh # -b后台运行


top
按数字1可看每个核心