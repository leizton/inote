export PS1='\n\n\[\033[1;31m\]\t \w]$(parse_git_branch)\[\033[0;m\] \n> '

# ag
agbin='/usr/local/bin/ag'
function af() {
  if [ $# -gt 1 ]; then
    find "$1" -name "*$2*"
  else
    find . -name "*$1*"
  fi
}
function def_m() {
  $agbin "$1(<.*>)?::$2\("
}
function ag() {
  $agbin -Q "$1"
}
function agi() {
  $agbin -sQ "$1"
}
function agw() {
  $agbin "\W$1\W"
}
function agwi() {
  $agbin "\W$1\W"
}
function agr() {
  $agbin "$1"
}
function agf() {
  $agbin "$1" -G "$2"
}
function _agr_cpp() {
  $agbin "$1" -G "h"
  $agbin "$1" -G "cc"
  $agbin "$1" -G "cpp"
}
function agclz() {
  _agr_cpp "struct .*$1"
  _agr_cpp "class .*$1"
}
function agd() {
  _agr_cpp ": public $1\W"
}
function agm() {
  _agr_cpp "\w+\->$1(<[\w:]+>)?\("
  _agr_cpp "\w+\.$1(<[\w:]+>)?\("
  _agr_cpp "^[\x20\t]+$1(<[\w:]+>)?\("
}
function weread() {
  "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --app="http://weread.qq.com"
}

# docker
# docker ps -a
# docker images
# docker exec -it $container_name /bin/bash
function docker_run() {
  container_name="$1"
  img_repo="$2"
  docker run -dit --name $container_name $img_repo /bin/bash
}
function docker_mount() {
  container_name="$1"
  img_repo="$2"
  src="$3"
  dst="$4"
  docker run -dit --name $container_name --mount type=bind,source=$src,target=$dst $img_repo /bin/bash
}

# docker util
function docker_practice() {
  docker_mount 'practice' 'whiker/ubuntu18_dev:v1.0' '/Users/whiker/sky/practice' '/opt/practice'
}

# time
function timest() {
  python ~/.mysh/timest.py $@
}

# util
alias ll="ls -ali";
alias ls="ls -G";
alias grep="grep -n --color";

alias lsdu='ls | xargs du -h -s';
alias lldu='ls -a | xargs du -h -s';
alias dus='du -h -s';

# xxnet
export XXNET_HOME='~/bin/XX-Net-3.8.7';
alias xxnetstart="cd $XXNET_HOME/ && sudo ./start";
alias xxnetstop="ps ax | grep -i python | grep -i launch | grep start.py | awk '{print $1}' | xargs sudo kill";

# mysql
alias mysqli='mysql -uroot -proot123 -P3306 --prompt "\u:\d> "';

# git
alias gits='git status'
alias gitph="git push";
alias gitphf="git push --force";
alias gitpl="git pull";
alias gitplr="git pull --rebase";
alias gith="git reset --hard HEAD";
function gita() {
  if [ $# -gt 0 ]; then
    git add $@
  else
    git add -A .
  fi
}
function gitphu() {
  curr_branch=`parse_git_branch`
  git push --set-upstream origin $curr_branch
}
function parse_git_branch() {
  b=`git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/\1/'`
  if [ -n "$b" ]; then
    b=" $b"
  fi
  echo "$b"
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
function gitlearn() {
  b=`git branch | grep learn | sed 's/^[ \t\*]*//g'`
  git checkout $b
}
function gitc() {
  git commit -m "$1"
}
function gitac() {
  git commit -a -m "$1"
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
function gitbrm() {
  gitb -D $1
  gitbd_remote $1
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
function gittest() {
  git commit --allow-empty -m '[feature-ok,ha3:master]'
  git push
}
function gittest1() {
  git commit --allow-empty -m '[feature-ok]'
  git push
}
function gitlzuser() {
  git config user.name 'leizton'
  git config user.email 'leizton@126.com'
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

function avro_gen_java() {
  java -jar ~/bin/avro/avro-tools-1.8.2.jar compile schema $1 $2
}

# PATH
export ROCKSDB_HOME="/usr/local/Cellar/rocksdb/5.14.2";
export PATH="$PATH:ROCKSDB_HOME/bin";

export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_172.jdk/Contents/Home";
export PATH="$PATH:$JAVA_HOME/bin";

export GOROOT="/Users/whiker/bin/go-1.10.2";
export GOPATH="/Users/whiker/bin/gopath";
export GOBIN="$GOROOT/bin";
export PATH="$PATH:$GOBIN";

export M2_HOME="/Users/whiker/bin/apache-maven-3.3.9";
export PATH="$PATH:$M2_HOME/bin";

export GRADLE_HOME="/Users/whiker/bin/gradle-3.2.1";
export PATH="$PATH:$GRADLE_HOME/bin";

export SCALA_HOME="/Users/whiker/bin/scala-2.12.1";
export PATH="$PATH:$SCALA_HOME/bin";

export CMAKE_HOME="/Users/whiker/bin/cmake-3.7.2";
export PATH="$PATH:$CMAKE_HOME/bin";

export SBT_HOME="/usr/local/Cellar/sbt/0.13.13";
export PATH="$PATH:$SBT_HOME/bin";

export MYSQL_HOME="/usr/local/mysql";
export PATH="$PATH:$MYSQL_HOME/bin";

export PROTOC_HOME="/Users/whiker/bin/protobuf-2.5.0";
export PATH="$PATH:$PROTOC_HOME/src";

alias python="/Library/Frameworks/Python.framework/Versions/3.5/bin/python3.5";
alias pip="/Library/Frameworks/Python.framework/Versions/3.5/bin/pip3.5";

export PKG_CONFIG_PATH="/Users/whiker/bin/libevent/lib/pkgconfig:$PKG_CONFIG_PATH";

export JULIA_HOME="/Applications/Julia-1.0.app/Contents/Resources/julia";
export PATH="$JULIA_HOME/bin:$PATH"

# user custom
