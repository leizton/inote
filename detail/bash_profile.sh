# util
alias vi_bash="vi ~/.bash_profile"
alias new_bash="source ~/.bash_profile"
alias ll="ls -ali";
alias ls="ls -G";
alias grep="grep --color";
alias lsdu="ls | xargs du -h -s";
alias lldu="ls -a | xargs du -h -s";
alias dus="du -h -s";

function now() {
  code="import time;from datetime import datetime"
  code="$code;tms=int(time.time()*1000)"
  code="$code;t=tms/1000"
  code="$code;s=datetime.fromtimestamp(float(t))"
  code="$code;s=s.strftime('%Y-%m-%d %H:%M:%S');print(t);print(tms);print(s)"
  python3 -c "$code"
}

function af() {
  find . -name "*$1*"
}

# hdfs
function hdfs() {
  command="$1"
  shift # 移除第一个参数
  case "$command" in
    "ls")
      hadoop fs -ls $*
      ;;
    "cp")
      hadoop fs -copyToLocal $*
      ;;
    "rm")
      hadoop fs -rm -r -f $*
      ;;
    "put")
      hadoop fs -put $*
      ;;
    *)
      echo "unsupport $command"
      ;;
  esac
}

# docker
# docker ps -a
# docker images
# docker exec -it $container_name /bin/bash
function docker_run() {
  container_name="$1"
  img_repo="$2"
  docker run -dit --net=host --name $container_name $img_repo /bin/bash
}
function docker_mount() {
  container_name="$1"
  img_repo="$2"
  src="$3"
  dst="$4"
  docker run -dit --net=host --name $container_name --mount type=bind,source=$src,target=$dst $img_repo /bin/bash
}

# docker util
function docker_start_cpp() {
  docker run -dit --net=host --name cpp \
      --security-opt seccomp=unconfined \
      --mount type=bind,source="/Users/awk/sky/practice/cpp",target="/opt/cpp" \
      leizton/udev:0.2 /bin/bash
}
function docker_in_cpp() {
  container_id=`docker ps | grep cpp | awk -F ' ' '{print $1}'`
  docker exec -it $container_id /bin/bash
}
function docker_start_torch() {
  docker run -dit --net=host --name torch \
      --security-opt seccomp=unconfined \
      --mount type=bind,source="/Users/awk/sky/pytorch",target="/opt/pytorch" \
      leizton/udev:0.2 /bin/bash
}
function docker_in_torch() {
  container_id=`docker ps | grep torch | awk -F ' ' '{print $1}'`
  docker exec -it $container_id /bin/bash
}
function docker_stop_torch() {
  docker stop torch
  docker rm torch
}

# git
alias gits='git status'
alias gitph="git push";
alias gitphf="git push --force";
alias gith="git reset --hard HEAD";
function gita() {
  if [ $# -gt 0 ]; then
    git add $@
  else
    git add -A .
  fi
}
function gitpl() {
  if [ $# -gt 0 ]; then
    git fetch origin $1
    git checkout $1
  else
    curr_branch=`parse_git_branch`
    git pull origin $curr_branch
  fi
}
function gitphu() {
  curr_branch=`parse_git_branch`
  git push --set-upstream origin $curr_branch
}
function parse_git_branch() {
  git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/\1/'
}
function gitl1() {
  num='5'
  if [ $# -gt 0 ]; then
    num=$1
  fi
  git log -$num --format="[%h %ci %cn] %s"
}
function gitl2() {
  if [ $# -lt 1 ]; then
    return 0
  fi
  num='5'
  if [ $# -gt 1 ]; then
    num=$2
  fi
  git log -$num --format="[%h %ci %cn] %s" -- $1
}
function gitch() {
  git checkout $@
}
function gitchm() {
  git checkout master
}
function gitc() {
  git commit -m "$1"
}
function gitac() {
  git commit -a -m "$1"
}
function gitacm() {
  git commit -a -m "compile"
}
function gitacmh() {
  git commit -a -m "compile"
  git push
}
function gitb() {
  git branch $@
}
function gitmr() {
  git merge $@
}
function gitmrm() {
  git merge master
}
function gitbd_remote() {
  git push origin --delete $1
}
function gitreset() {
  git reset --hard $1
}
function gitreset1() {
  git reset --hard HEAD^
}
function gitrebase() {
  git rebase -i $1
}
function gitrebase2() {
  git rebase -i HEAD^^
}
function gitdf() {
  git diff --stat $@
}
function gitdfm() {
  git diff --stat master
}
function gitlzuser() {
  git config user.name 'leizton'
  git config user.email 'leizton@126.com'
}

# user custom
function gitlzuser() {
  git config user.name 'leizton'
  git config user.email 'leizton@126.com'
}

export PATH="/usr/local/bin:$PATH"

# bash
# export PS1='\n\n\[\033[1;31m\]\t $(pwd)] $(parse_git_branch)\[\033[0;m\]\n> '

# zsh
function newline1() { tm=`date +"%A %d %H:%M:%S"`; echo "\n$tm" }
function newline2() { echo "\n>" }
setopt PROMPT_SUBST
autoload -U colors && colors
# 一定要用单引号, 不能用双引号
export PROMPT='%F{red} $(newline1) %~] $(parse_git_branch) $(newline2) %f'
