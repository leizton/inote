export PS1='\[\033[1;31m\]\t \w]$(parse_git_branch) \$\[\033[0;m\] '
export USER_NAME="leizton"
export USER_HOME="~"

alias refresh_bash='source ~/.bash_profile'

# docker
function dock_start() {
  docker start "$1"
}
function dock_rm() {
  docker stop "$1" && docker rm "$1"
}
function dock_in() {
  docker exec -it "$1" /bin/bash
}
function dock_run() {
  docker run -dit --name "$1" "$USER_NAME/ubuntu16:$2" /bin/bash
}
function dock_mount() {
  docker run -dit --name tmp --mount type=bind,source=$1,target=$2 $USER_NAME/ubuntu16:$3 /bin/bash
}
alias dock_ps='docker ps -a'
alias dock_img='docker images'

# docker util
alias dock_practice="dock_mount '$USER_HOME/sky/practice' '/opt/practice' dev"

# time
function timest() {
  python ~/.mysh/timest.py $@
}

# util
alias ll="ls -ali";
alias ls="ls -G";
alias grep="grep --color";

alias lsdu='ls | xargs du -h -s';
alias lldu='ls -a | xargs du -h -s';
alias dus='du -h -s';

# xxnet
export XXNET_HOME='~/bin/XX-Net-3.8.7';
alias xxnetstart="cd $XXNET_HOME/ && sudo ./start";
alias xxnetstop="ps ax | grep -i python | grep -i launch | grep start.py | awk '{print $1}' | xargs sudo kill";

# mysql
alias mysqli='mysql -uroot -proot123 --prompt "\u:\d>"';

# git
alias gits="git status";
alias gitl="git log --oneline";
alias gita="git add -A .";
alias gitph="git push";
alias gitpl="git pull";
alias gitplr="git pull --rebase";
alias gitb="git branch";
alias gith="git reset --hard HEAD";
alias gituser="git config user.name";
alias gitemail="git config user.email";
alias gitconf="git config user.name fanyaoqun && git config user.email fanyaoqun@bytedance.com";
function parse_git_branch() {
  b=`git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/\1/'`
  if [ -n "$b" ]; then
    b=" $b"
  fi
  echo "$b"
}
function gitc() {
  git commit -m "$1"
}
function gitac() {
  git commit -a -m "$1"
}
function gitbd_remote() {
  git push origin --delete $1
}
function gitbmst() {
  if [ $# -eq 1 -a $1 == '-h' ]; then
    echo 'gitbmst'
    echo 'gitbmst *.java'
    echo 'gitbmst -au Jack.Huang'
    echo 'gitbmst *.java Jack.Huang'
    return
  fi

  if [ $# -eq 0 ]; then
    find . -name "*.java" | xargs -n1 git blame --line-porcelain | sed -n 's/^author //p' | sort | uniq -c | sort -rn
  elif [ $# -eq 1 ]; then
    find . -name "$1" | xargs -n1 git blame --line-porcelain | sed -n 's/^author //p' | sort | uniq -c | sort -rn
  elif [ $# -eq 2 ]; then
    if [ "$1" = "-au" ]; then
      /bin/bash ~/.mysh/git_blame_stat.sh *.java "$2"
    else
      /bin/bash ~/.mysh/git_blame_stat.sh $@
    fi
  fi
}

# 环境变量
PATH="/Library/Frameworks/Python.framework/Versions/3.5/bin:${PATH}";
export PATH;

export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home";

export M2_HOME="$USER_HOME/bin/apache-maven-3.3.9";
export PATH="$PATH:$M2_HOME/bin";

export GRADLE_HOME="$USER_HOME/bin/gradle-3.2.1";
export PATH="$PATH:$GRADLE_HOME/bin";

export SCALA_HOME="$USER_HOME/bin/scala-2.12.1";
export PATH="$PATH:$SCALA_HOME/bin";

export CMAKE_HOME="$USER_HOME/bin/cmake-3.7.2";
export PATH="$PATH:$CMAKE_HOME/bin";

export SBT_HOME="/usr/local/Cellar/sbt/0.13.13";
export PATH="$PATH:$SBT_HOME/bin";

export MYSQL_HOME="/usr/local/mysql";
export PATH="$PATH:$MYSQL_HOME/bin";

alias python="/Library/Frameworks/Python.framework/Versions/3.5/bin/python3.5";
alias pip="/Library/Frameworks/Python.framework/Versions/3.5/bin/pip3.5";

export PKG_CONFIG_PATH="$USER_HOME/bin/libevent/lib/pkgconfig:$PKG_CONFIG_PATH";
