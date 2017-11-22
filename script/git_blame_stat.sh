if [ $# -eq 2 ]; then
	file_pattern=$1
	author=$2
else
	echo 'param num error'
	exit 1
fi

gitdir_name=`pwd | rev | awk -F \/ '{print $1}' | rev`
tmp_file="/tmp/gbmstat_$gitdir_name.gbmstat"
if [ -f $tmp_file ]; then
	echo "please rm $tmp_file first"
	exit 1
fi

sum_line_num=0
files=`find $gitdir -name "$file_pattern"`
line_num_and_file=""
for file in $files
do
	line_num=`git blame --line-porcelain $file | \
		sed -n 's/^author //p' | \
		grep "$author" | wc -l`
	if [ $line_num -eq 0 ]; then
		continue
	fi
	let "sum_line_num=$sum_line_num+$line_num"
	echo "${line_num} ${file}" >> $tmp_file
done

if [ $sum_line_num -gt 0 ]; then
	sort -n -k 1 $tmp_file
	rm $tmp_file
fi
echo $sum_line_num
