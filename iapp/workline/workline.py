#! /usr/bin/python
# coding=utf-8

import sys
import os
import time


TASK_ID_LEN = 4
MAX_TASK_ID = 10**TASK_ID_LEN - 1

EVENT_NAME_LEN = 6
EVENT_CREATE   = 'create'
EVENT_START    = 'start'
EVENT_MARK     = 'mark'
EVENT_DONE     = 'done'
EVENT_END      = 'end'

TASK_UNKNOW = 0
TASK_TODO   = 1
TASK_DOING  = 2
TASK_DONE   = 3
TASK_END    = 4
TASK_STATE_STRS = ['unknow', 'todo', 'doing', 'done', 'end']


class GlobalVar:
    def __init__(self):
        self.root_dir = ''
        self.curr_repo = ''  # repo fullpath
        self.curr_wline = ''  # wline name
        self.tasks = [None]

globalv = GlobalVar()


class Event:
    def __init__(self, time, name, content):
        self.time = time
        self.name = name
        self.content = content

class Task:
    def __init__(self, id, name):
        self.id = id
        self.name = name
        self.events = []
        self.state = TASK_UNKNOW
    def appendRawEvent(self, raw_event):
        event = Event(raw_event[1], raw_event[2], raw_event[4])
        self.events.append(event)
        if event.name == EVENT_CREATE:
            self.state = TASK_TODO
        elif event.name == EVENT_START:
            self.state = TASK_DOING
        elif event.name == EVENT_DONE:
            self.state = TASK_DONE
        elif event.name == EVENT_END:
            self.state = TASK_END


def catpath(*parts):
    path = parts[0]
    for s in parts[1:]:
        if not path.endswith('/'):
            path += '/'
        path += s
    return(path)

def readfile(file_fullpath):
    ret = []
    f = os.popen('cat ' + file_fullpath)
    for l in f:
        l = l.strip()
        if len(l) > 0:
            ret.append(l)
    f.close()
    return ret

def writefile(file_fullpath, line, is_append):
    if is_append:
        opt = 'a'
    else:
        opt = 'w'
    f = open(file_fullpath, opt)
    f.write(line + '\n')
    f.close()

def currTimeFmtStr():
    return time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()))

def fillstr(s, fixlen, fill):
    while len(s) < fixlen:
        s += fill
    return s


def currRepoMetaFile():
    global globalv
    return catpath(globalv.root_dir, '.curr_repo')

def setCurrRepo(repo_name):
    global globalv
    fullpath = catpath(globalv.root_dir, repo_name)
    globalv.curr_repo = fullpath
    writefile(currRepoMetaFile(), globalv.curr_repo, False)

def currWlineMetaFile():
    global globalv
    return catpath(globalv.root_dir, '.curr_wline')

def getWlineFilePath(wline_name):
    global globalv
    return catpath(globalv.curr_repo, wline_name + '.line')

def currWlineFile():
    global globalv
    return getWlineFilePath(globalv.curr_wline)

def setCurrWline(wline_name):
    global globalv
    globalv.curr_wline = wline_name
    writefile(currWlineMetaFile(), wline_name, False)

def recordEvent(task_id, task_name, event_name, event_content):
    taskid_str = fillstr(str(task_id), 4, ' ')
    event = taskid_str + ' [' + currTimeFmtStr() + '] ' + fillstr(event_name, 6, ' ') + ' [' + task_name + '] ' + event_content
    writefile(currWlineFile(), event, True)

def parseEventLine(line):
    ret = [line[0:TASK_ID_LEN]];
    ret.append(line[6:25])
    ret.append(line[27:33].strip())
    idx = line.find(']', 35)
    ret.append(line[35:idx])
    ret.append(line[(idx+2):])
    return ret

def loadTasks():
    wline_file = currWlineFile()
    if not os.path.isfile(wline_file):
        return
    tasks = [None]
    for l in readfile(wline_file):
        l = l.strip()
        if l.startswith('#') or l == '':
            continue
        raw_event = parseEventLine(l)
        task_id = int(raw_event[0])
        task = None
        if task_id < len(tasks):
            task = tasks[task_id]
        elif task_id == len(tasks):
            tasks.append(None)
        else:
            while task_id >= len(tasks):
                tasks.append(None)
        if task == None:
            task = Task(task_id, raw_event[3])
            tasks[task_id] = task
        task.appendRawEvent(raw_event)
    return tasks

def printTask(task):
    event = task.events[-1]
    fmt = '\033[31m#%-4d\033[0m  '
    fmt += event.time + ' %-6s  '
    fmt += '\033[31m' + task.name + '\033[0m  '
    fmt += event.content
    print(fmt % (task.id, event.name))

def printTaskDetail(task):
    print('\033[31m#%-4d [%s]\033[0m' % (task.id, task.name))
    print('----------------------------')
    for event in task.events:
        print('%s  %-7s> %s' % (event.time, event.name, event.content))

def appInit():
    global globalv
    home_dir = os.environ['HOME']
    globalv.root_dir = catpath(home_dir, '.workline')
    if not os.path.isdir(globalv.root_dir):
        os.mkdir(globalv.root_dir)
    #
    if os.path.isfile(currRepoMetaFile()):
        globalv.curr_repo = readfile(currRepoMetaFile())[0]
    #
    if os.path.isfile(currWlineMetaFile()):
        globalv.curr_wline = readfile(currWlineMetaFile())[0]
        if globalv.curr_wline != '':
            globalv.tasks = loadTasks()

def initRepo(url):
    global globalv
    repo_name = url[url.rfind('/')+1:]
    if repo_name == '':
        print('invalid url: ' + url)
        return
    if repo_name.endswith('.git'):
        repo_name = repo_name[0:len(repo_name) - 4]

    fullpath = catpath(globalv.root_dir, repo_name)
    ret = -1
    if not os.path.isdir(fullpath):
        ret = os.system('cd ' + globalv.root_dir + ' && git clone ' + url)
    else:
        ret = os.system('cd ' + fullpath + ' && git pull --rebase')
    if ret == 0:
        setCurrRepo(repo_name)

def status():
    ret = os.system('cd ' + globalv.curr_repo + ' && git status')

def pull():
    ret = os.system('cd ' + globalv.curr_repo + ' && git pull --rebase')

def push():
    commit = 'mod'
    if globalv.curr_wline != '':
        commit += ':' + globalv.curr_wline
    ret = os.system('cd ' + globalv.curr_repo + ' && git add -A . && git commit -m ' + commit + ' && git pull --rebase && git push')

def switchWline(wline_name):
    wline_file = getWlineFilePath(wline_name)
    if not os.path.isfile(wline_file):
        os.system('echo "#" > ' + wline_file)
    setCurrWline(wline_name)

def renameWline(oldname, newname):
    oldfile = getWlineFilePath(oldname)
    if not os.path.isfile(oldfile):
        print(oldname + ' not exist')
        return
    newfile = getWlineFilePath(newname)
    if os.path.isdir(newfile) or os.path.isfile(newfile):
        print(newfile + 'already existed')
        return
    if os.system('mv ' + oldfile + ' ' + newfile) == 0:
        if oldname == globalv.curr_wline:
            switchWline(newname)

def createTask(task_name):
    global globalv
    recordEvent(len(globalv.tasks), task_name, EVENT_CREATE, '')

def modTask(event_name, task_id, content):
    global globalv
    task_id = int(task_id)
    if task_id < len(globalv.tasks):
        task = globalv.tasks[task_id]
        recordEvent(task.id, task.name, event_name, content)

def lsWline():
    global globalv
    if globalv.curr_repo == '':
        print("current repo hasn't been setted")
        return
    cmd = os.popen('ls -1 ' + globalv.curr_repo)
    for wline in cmd:
        wline = wline.strip()
        if len(wline) < 6 or not wline.endswith('.line'):
            continue
        wline = wline[0:len(wline) - 5]
        if wline == globalv.curr_wline:
            print('\033[31m# ' + wline + '\033[0m')
        else:
            print(wline)
    cmd.close()

def showTasks():
    lsTasks(TASK_TODO)
    lsTasks(TASK_DOING)

def lsTasks(state):
    global globalv
    print('\033[31m# ' + TASK_STATE_STRS[state] + '\033[0m')
    print('--------------------------------')
    for task in globalv.tasks:
        if not task == None and task.state == state:
            printTask(task)
    print('')

def llTask(task_id):
    global globalv
    if task_id < len(globalv.tasks):
        task = globalv.tasks[task_id]
        printTaskDetail(task)

if __name__ == '__main__':
    appInit()
    argv = sys.argv
    arg_num = len(argv)
    if arg_num < 2:
        exit(0)
    if argv[1] == 'help':
        print("|----------------------------------------------")
        print("|  init         repo-url")
        print("|  curr-repo")
        print("|  status")
        print("|  pull")
        print("|  push")
        print("|----------------------------------------------")
        print("|  switch       new_or_old_wline_name")
        print("|  curr-wline")
        print("|  rename       old_wline_name  new_wline_name")
        print("|  ls-wline")
        print("|----------------------------------------------")
        print("|  create      task-name")
        print("|  mark        task-id  content")
        print("|  start       task-id  content")
        print("|  done        task-id  content")
        print("|  end         task-id  content")
        print("|----------------------------------------------")
        print("|  ls")
        print("|  ll          task-id")
        print("|  ls-todo")
        print("|  ls-doing")
        print("|  ls-done")
        print("|----------------------------------------------")
    elif argv[1] == 'init' and arg_num == 3:
        initRepo(argv[2])
    elif argv[1] == 'curr-repo':
        print(globalv.curr_repo[globalv.curr_repo.rfind('/')+1:])
    elif argv[1] == 'status':
        status()
    elif argv[1] == 'pull':
        pull()
    elif argv[1] == 'push':
        push()
    elif argv[1] == 'switch' and arg_num == 3:
        switchWline(argv[2])
    elif argv[1] == 'curr-wline':
        print('\033[31m# ' + globalv.curr_wline + '\033[0m')
    elif argv[1] == 'rename' and arg_num >= 3:
        if arg_num == 3:
            renameWline(globalv.curr_wline, argv[2])
        else:
            renameWline(argv[2], argv[3])
    elif argv[1] == 'ls-wline':
        lsWline()
    elif argv[1] == 'ls':
        showTasks()
    elif argv[1] == 'ls-todo':
        lsTasks(TASK_TODO)
    elif argv[1] == 'ls-doing':
        lsTasks(TASK_DOING)
    elif argv[1] == 'ls-done':
        lsTasks(TASK_DONE)
    elif argv[1] == 'll' and arg_num == 3:
        llTask(int(argv[2]))
    elif arg_num >= 3:
        content = ''
        if argv[1] == EVENT_CREATE:
            content = argv[2]
        if arg_num > 3:
            for i in xrange(3, len(argv)):
                content += ' ' + argv[i]
        content = content.strip()
        if argv[1] == EVENT_CREATE:
            createTask(content)
        elif argv[1] == EVENT_START:
            modTask(EVENT_START, argv[2], content)
        elif argv[1] == EVENT_MARK:
            modTask(EVENT_MARK, argv[2], content)
        elif argv[1] == EVENT_DONE:
            modTask(EVENT_DONE, argv[2], content)
        elif argv[1] == EVENT_END:
            modTask(EVENT_END, argv[2], content)
    else:
        print('no command: ' + argv[1])
