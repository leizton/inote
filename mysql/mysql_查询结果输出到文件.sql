mysql在本机
	> select * from table into outfile '/tmp/a.txt';

mysql在远程机
	$ mysql -u"Username" -p"Password" -D"Database" -h"host域名或IP" -P"端口,默认3306" -e"sql语句" > '/tmp/a.txt'