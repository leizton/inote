#!/usr/bin/env python
# encoding=utf-8

import datetime
import time
import random
from threading import Thread


class Task(Thread):
  def __init__(self, taskId, count):
    super(Task, self).__init__()
    self.taskId = taskId
    self.count = count
    self.sum = 0

  def run(self):
    while self.count > 0:
      self.sum += self.count
      self.count -= 1
      time.sleep(random.randint(1, 3))
      print('th-%s: %s' % (self.taskId, datetime.datetime.now().strftime('%H-%m-%s')))

  def getId(self):
    return self.taskId

  def getResult(self):
    return self.sum


tasks = []
for i in range(4):
  t = Task(i, 3)
  t.start()
  tasks.append(t)
for task in tasks:
  task.join()
  print('th-%s ret: %s' % (task.getId(), task.getResult()))
