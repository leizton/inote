jvm总内存大小 = 年轻代大小 + 老年代大小 + 持久代大小(一般固定64MB)

java -Xmx3g -Xms3g -Xmn1500m -Xss128k
-Xmx3g     最大内存3g
-Xms3g     初始内存3g，一般设成和-Xms一样，避免垃圾回收完成后jvm重新分配内存
-Xmn1500m  年轻代大小1500m
-Xss128k   每个线程的堆栈大小

java -Xmx3g -Xms3g -Xss128k -XX:NewRatio=4 -XX:SurvivorRatio=4 -XX:MaxPermSize=64m -XX:MaxTenuringThreshold=4
-XX:NewRatio=4       年轻代与老年代的比例是1:4，即年轻代占jvm总内存的1/5
-XX:SurvivorRatio=4  年轻代的2个Survivor之和与1个Eden区的比例是2:4，即1个Survivor区占年轻代的1/6
-XX:MaxPermSize=64m  持久代64m
-XX:MaxTenuringThreshold=4  年轻代在Survivor里经过多少次后会进入老年代