#! /bin/sh
# ./cutlog.sh "\[2016-10-26 14:" "\[2016-10-26 15:" catalina.out ret.log

from=`grep -nm1 "$1" $3 | awk -F: '{print $1}'`  #起始行号
to=`grep -nm1 "$2" $3 | awk -F: '{print $1}'`  #结束行号
total=`wc -l $3 | awk '{print $1}'`  #文件总行数

# 截取行数
num="$to - $from"
num=`expr $num`
num="$num + 1"
num=`expr $num`

tailFirst="$total - $from"
tailFirst=`expr $tailFirst`
tailFirst="$tailFirst + 1"
tailFirst=`expr $tailFirst`

if [ $to -le $tailFirst ]; then
	`head -$to $3 | tail -$num > $4`
else
	`tail -$tailFirst | head -$num > $4`
fi