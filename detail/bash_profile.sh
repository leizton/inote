export PS1='\[\033[1;31m\]\t \w]$(parse_git_branch)\[\033[0;m\] \n> '

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
alias gits="git status";
alias gitl="git log --oneline";
alias gita="git add -A .";
alias gitph="git push";
alias gitpl="git pull";
alias gitplr="git pull --rebase";
alias gith="git reset --hard HEAD";
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
  git log -$num --format="%H  %cn  %s"
}
function gitch() {
  git checkout $@
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
