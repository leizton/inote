> 如果vi出问题, 直接sudo apt-get install vim

# 配置
在home目录下创建.vimrc, 写入如下内容
set nu              "行号
set tabstop=2       "tab占2个空格
set expandtab       "用空格代替tab
set shiftwidth=4    "指定自动缩进时使用的空白长度, 和tabstop相同
set autoindent      "自动缩进
set encoding=utf-8  "UTF-8编码
set hlsearch        "高亮查询结果
syntax on           "语法高亮

# 基本命令
- u       撤销

# 文本替换
格式：[行范围选择]s/源字符串/目标字符串[/g行内范围选择]
例如：
- :s/hello/hi         当前行的第一个hello替换成hi
- :s/hello/hi/g       当前行的所有hello替换成hi
- :3,7s/hello/hi/g    第3~7行
- :3,$s/hello/hi/g    第3行~最后一行
- :.,7s/hello/hi/g    当前行~第7行
- :%s/hello/hi/g      %表示整个文件，等效于1,$

#
:set nonu  关闭行号

# 粘贴时不自动缩进
- :set paste            //进入粘贴模式
  :set nopaste          //退出粘贴模式
- set pastetoggle=<F9>  //设置按F9切换粘贴和非粘贴模式