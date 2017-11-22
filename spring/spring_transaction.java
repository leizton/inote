0. mysql的事务处理
> select @@tx_isolation;  // 查看当前设置的事务隔离级别
set autocommit=0;
start transaction;  // 开始一个事务
insert into users(age) values(20) where name='Jack';
> commit;    // 提交事务结束
> rollback;  // 回滚结束

1. ANSI SQL 定义的4种事务隔离级别
未提交读，提交读，可重复读，可序列化

2. 脏读、不可重复读、幻读
脏读       读取了事务修改但未提交的数据
不可重复读  一个事务有多次重复某一行读取，如果想"合理地确保"每次读到的是相同结果，则持有行级读锁。可重复读级别可以合理确保。
幻读       合理确保的可重复读不包括再次读取表时出现了新的一行或丢失了一行，此时需要表锁或串行执行事务
> 不可重复读的场景
    t0. 事务A中，读取张三的工资1000
    t1. 事务B中，张三的工资修改成2000，并提交了事务
    t3. 事务A中，再次读取张三的工资是2000
> 幻读的场景
    读取包括一个"where status=1"的筛选行选择条件，并行执行的另一个事务修改某些行的status值

3. spring的事务隔离级别
ISOLATION_READ_UNCOMMITTED  一个事务可以读取其他事务修改但未提交的数据，该级别不能防止脏读，不推荐使用
ISOLATION_READ_COMMITED     一个事务只能读其他事务已经提交的数据，可以防止脏读
ISOLATION_DEFAULT           默认值，等于ISOLATION_READ_COMMITED
ISOLATION_SERIALIZABLE      所有事务串行执行，使得事务间完全不可能产生干扰，可防止脏读、不可重复读、幻读，该级别性能很差

4. spring的事务传播行为
PROPAGATION_REQUIRED        if 当前存在事务，加入当前事务；else 新建一个事务运行
PROPAGATION_REQUIRES_NEW    if 当前存在事务，挂起当前事务；然后创建新的事务运行
PROPAGATION_SUPPORTS        if 当前存在事务，加入当前事务；else 以非事务方式运行
PROPAGATION_NOT_SUPPORTED   if 当前存在事务，挂起当前事务；然后以非事务方式运行
PROPAGATION_NEVER           if 当前存在事务，抛出异常；else 以非事务方式运行
PROPAGATION_MANDATORY       if 当前存在事务，加入当前事务；else 抛出异常
PROPAGATION_NESTED          if 当前存在事务，创建一个当前事务的嵌套事务来运行；else 新建一个事务运行
> 常见场景
    serviceA.methodA已经运行了一个事务，methodA调用serviceB.methodB，methodB里也有一个事务
    对于PROPAGATION_REQUIRED，methodB并不创建新事务，而是加入methodA的事务运行

5. spring的事务管理
"按照给定的事务规则来执行提交或回滚操作"
TransactionDefinition       定义事务规则
PlatformTransactionManager  执行提交或回滚操作
TransactionStatus           表示运行中的事务的状态
> 打一个不恰当的比喻，TransactionDefinition TransactionStatus 的关系就像程序和进程的关系

6. 基于spring的底层API的事务编程
@Service class BankService {
    @Resource BankDao bankDao;
    @Resource TransactionDefinition txDefinition;
    @Resource PlatformTransactionManager txManager;

    boolean transferAccounts(long fromUserId, long toUserId, double amount) {
        TransactionStatus txStatus = txManager.getTransaction(txDefinition);
        boolean success = false;
        try {
            success = bankDao.transferAccounts(fromUserId, toUserId, amount);
            txManager.commit(txStatus);
        } catch (Exception) {
            success = false;
            txManager.rollback(txStatus);
        }
        return success;
    }
}

7. 基于spring的TransactionTemplate的事务编程
@Service class BankService {
    @Resource BankDao bankDao;
    @Resource TransactionTemplate txTemplate;

    boolean transferAccounts(long fromUserId, long toUserId, double amount) {
        return (Boolean) txTemplate.execute(new TransactionCallback() {
            @Override object doInTransaction(TransactionStatus status) {
                try {
                    return bankDao.transferAccounts(fromUserId, toUserId, amount);
                } catch (Exception e) {
                    status.setRollbackOnly();
                    return false;
                }
            }
        });
    }
}

8. 基于Transaction注解的事务编程
// spring.xml配置
<tx:annotation-driven />
//
@Transactional(propagation=Propagation.REQUIRED)
boolean transferAccounts(long fromUserId, long toUserId, double amount) {
    return bankDao.transferAccounts(fromUserId, toUserId, amount);
}
