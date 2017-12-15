- Perm 永久区 (java8废弃，用元空间替代)
- Heap 堆内存
  - OldGen 老年代
  - YoungGen 年轻代
    1. Eden
	  2. Survivor1
	  3. Survivor2

-Xms         设置初始堆大小
-Xmx         设置堆大小的最大值
-XX:NewSize  设置YoungGen的大小

Major Gc 在OldGen区
Minor Gc 在YoungGen区


# 引发full gc的情形
1. 调用System.gc()
2. java.lang.OutOfMemoryError: Java heap space 
   Heap-空间不足
3. java.lang.OutOfMemoryError: PermGen space
   Perm-空间不足
4. CMS GC-时出现promotion failed和concurrent mode failure
   OldGen-空间不足
5. others


# 查看java进程堆大小
sudo jmap -heap ${PID}

# gc日志

## 例子
        引起gc的原因        发生在年轻代   回收前年轻代大小   回收后年轻代大小(年轻代总大小)  回收前堆大小   回收后堆大小(堆总大小)
[GC (Allocation Failure) [PSYoungGen:    577949K     ->     9024K(606720K)      ]   1210153K -> 641427K(2004992K),  0.0166054 secs] [Times: user=0.04 sys=0.00, real=0.01 secs]

## 分析
从上面例子看，年轻代回收 577949-9024=568925，堆回收 1210153-641427=568726
所以有 568925-568726=199 进入老年代

> http://blog.csdn.net/column/details/14851.html
> https://yq.aliyun.com/articles/48957