$ tmux  创建新会话
$ tmux ls  有哪些会话
	0: 2 windows (created Tue Mar  8 10:23:14 2016) [145x40]
	1: 1 windows (created Tue Mar  8 10:37:35 2016) [145x40]
$ tmux attach-session -t 1  进入1号会话

前缀 Ctrl+b

s  session, 以菜单方式选择会话
d  退出当前会话

# 窗口
c  创建新窗口
w  窗口列表
n  下一个窗口
p  上一个窗口
l  上一次使用的窗口
3  跳至第3个窗口
&  关闭窗口

# 面板
上下左右键切换面板
%  左右分屏
"  上下分屏
o  下一个面板
x  关闭面板

按住Ctrl+b, 按上下左右键, 调节面板大小

# 终端里向上翻页
fn+上下键(PageUp/PageDown)  按q或ecs退出