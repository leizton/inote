# 常见问题
1. unable to access 'https://github.com/user/project.git': The requested URL returned error: 403
   把远程仓库url改成'https://user@github.com/user/project.git'

# 合并commit
https://www.jianshu.com/p/964de879904a
合并完成后用 git push --force 提交到远程分支上

# tag
git tag                              # 列出当前所有的tag
git checkout -b newBranch tagA       # 从tagA检出新分支
git tag -a 'v0.1' -m 'version 0.1'   # 创建tag
git log --pretty=oneline ^tagA tagB  # 查看两个tag间的commit

# github验证用户权限
ssh -T git@github.com

# sshkey
ssh-keygen -t rsa -C 'xxx@yyy.com'

# base
git保存快照, svn保存文件的变化, 所有在切换分支时git速度比svn快
git用文件内容通过SHA-1 hash生成提交版本号, svn使用递增编号作版本号
git的3种文件状态: modified, staged, commited
  已修改(工作区) --add--> 已暂存(暂存区) --commit--> 已提交(本地仓库)

# 工作区 暂存区 版本库
git add				工作区 -> 暂存区
git commit			暂存区 -> 版本库
git reset			版本库 -> 暂存区
git checkout		暂存区 -> 工作区
git reset --hard	版本库 -> 暂存区 -> 工作区

# 查看所有分支, 包括远程分支
git branch -a

# 删除远程分支
git push origin --delete 分支名

# 拉取远程分支
git pull包含 git fetch(把远程仓库的某个分支的HEAD拉取到本地) 和 git merge(把其他分支的HEAD合并到当前分支) 2个步骤
格式：git pull 远程仓库名 远程分支[:同时要合并的另外一个本地分支]
如果指定了--rebase，则不是执行git merge，而是执行git rebase
git pull origin master:local  把远程分支master拉取下来后合并到本地的local分支，当前分支也合并了远程的master分支
git pull origin master:master

# 查看远程仓库设置
git remote -v

# clone
git clone 地址 -b master  # 只clone下master分支

# remote
git remote add origin git@gitlab...(http://gitlab...)  # git@在push时无需输密码, http://需要
git remote                # 查看远程仓库
git remote remove origin  # 删除origin仓库
git remote -v             # 查看远程仓库的地址

# log/reflog
log只显示commit操作记录, reflog还会显示checkout切换分支的操作记录
git log --graph --oneline

# diff
git diff                       # 默认工作区和暂存区的差别
git diff --cached(或--staged)  # 暂存区和本地仓库的差别
git show commit1               # 查看commit1的提交内容
git diff commit1^              # 效果同上, 比较commit1和commit1的前一次提交
git diff commit1 commit2       # 显示两次提交间的差别
git diff master branch1        # 两个分支间的差别

# push
git push -u origin branch1     # 绑定远程仓库的branch1分支, 以后直接push会推到branch1分支

# fetch/merge/pull
git fetch                 # 拉取远程分支到本地
git merge origin/branch1  # 把远程branch1与本地合并
git pull                  # 合并上面两个命令
git pull --rebase
git merge --abort         # 取消conflict合并

# rebase和merge的区别: rebase线性合并, merge分叉合并
合并前
		  E---F  #本地master分支
		 /
	A---B---C---D  #origin/master，远程分支
git merge master
		  E-------F
		 /         \
	A---B---C---D---G  # master和origin/master，创建了一个新的commit(G)
git rebase master
	A---B---C---D---E1---F1  # master和origin/master，E1 F1和E F的commit SHA序号不同

# branch
git branch dev  # 创建新分支(dev)
git branch -a  # 显示本地和远程的所有分支

# checkout
git checkout [HEAD] $file    # 已修改的工作区文件回退到当前提交
git checkout 2588f237 $file  # 回退到版本2588f237
git checkout -b dev          # 创建新分支，并切换新分支

# reset
git reset --soft  HEAD~  # 把HEAD指针指向上一个commit
git reset --mixed HEAD~  # 把本地仓库的HEAD~_commit copy to staged区
git reset --hard  HEAD~  # 把本地仓库的HEAD~_commit copy to工作区和staged区, 覆盖了当前修改

# revert
git revert HEAD~2  # 和git reset --hard HEAD~2的区别是: revert会保留HEAD HEAD~这两次commit然后创建新的commit来恢复, reset --hard不会保留

# cherry-pick
git cherry-pick commitID  # 把其他分支的某个提交应用到当前分支, 仅应用这一个提交

# blame
git blame Test.java  # 显示每行代码最后是谁在什么时间提交的
find . -name "*.java" | xargs -l git blame --line-porcelain | sed -n 's/^author //p' | sort | uniq -c | sort -rn
find . -name "*.java" | xargs -l git blame | grep -n "作者名"  # 查找某个人写的每行代码
# 查找某个人写了哪些文件
comm -12 <(find . -name "*.java" | sort) <(find . -name "*.java" | \
	xargs -l git blame --line-porcelain | \
	sed -n 's/^\(author\|filename\) //p' | \
	awk '{if(NR%2){printf $0 " "}else{printf $0 "\n"}}' | \
	grep "作者" | awk '{print $2}' | sort | uniq | awk '{print "./" $0}')

# 生成凭据, 下次免输入密码
git config credential.helper store

# 忽略文件权限
git config core.filemode false

# 设置和取消代理
git config --global http.proxy socks5://127.0.0.1:1080 && git config --global https.proxy socks5://127.0.0.1:1080
git config --global --unset http.proxy && git config --global --unset https.proxy