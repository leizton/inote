定时任务的cron表达式
"0 0 3 * * ?"    每天的3:00:00执行
"*/1 * * * * ?"  每秒

字段    是否必须          取值             特殊字符
秒        YES           0-59             , - * /
分        YES           0-59             , - * /
时        YES           0-23             , - * /
日        YES           1-31             , - * ? / L W
月        YES      1-12 or JAN-DEC       , - * /
星期      YES       1-7 or SUN-SAT       , - * ? / L #
年        NO       empty, 1970-2099      , - * /

# 设置定时任务
# 写入 0 12 * * 5 /bin/bash run.sh (格式 m分钟 h小时 dom日 mon月 dow星期 command命令)
$ crontab -e

# 查看写入的定时任务
$ crontab -l

# 重新启动cron服务
$ sudo service cron restart