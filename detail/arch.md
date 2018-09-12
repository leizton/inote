# a simple distributed arch
- meta_service
  base on a kv store
  coordinate by raft
  as an admin role
  provide route config & query
- multicenter service
  multi master-slave struct

# available
MTBF: mean time between failures. 平均多久出一次故障
MTTR: mean time to recover. 出故障时平均恢复时间
availability = f(MTBF, MTTR)
- solution
  多副本冗余. 数量N+2, 发布时仍有两副本; 副本间对等;
  流量控制, 服务熔断, 服务降级
  管理变更. 补全完整的线下测试; 快速清晰的回滚支持,包括代码和数据; 灰度发布;
  维护阶段的系统, 新功能应多检查多测试慢上线, 防止踩了已有代码的坑
  小功能, 多迭代