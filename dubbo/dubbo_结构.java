服务接口层（Service）
	该层是与实际业务逻辑相关的，根据服务提供方和服务消费方的业务设计对应的接口和实现。

配置层（Config）
	对外配置接口，以ServiceConfig和ReferenceConfig为中心，可以直接new配置类，也可以通过spring解析配置生成配置类。

服务代理层（Proxy）
	服务接口透明代理，生成服务的客户端Stub和服务器端Skeleton。
	以ServiceProxy为中心。
	扩展接口: ProxyFactory。

服务注册层（Registry）
	封装服务地址的注册与发现，以服务URL为中心。
	扩展接口: RegistryFactory、Registry和RegistryService。

集群层（Cluster）
	封装多个提供者的路由及负载均衡，并桥接注册中心。
	将多个服务提供方组合为一个服务提供方，实现对服务消费方来透明，只需要与一个服务提供方进行交互。
	以Invoker为中心。
	扩展接口: Cluster、Directory、Router和LoadBalance。

监控层（Monitor）
	RPC调用次数和调用时间监控，以Statistics为中心。
	扩展接口: MonitorFactory、Monitor和MonitorService。

远程调用层（Protocol）
	封将RPC调用，以Invocation和Result为中心。
	扩展接口: Protocol、Invoker、Exporter。

信息交换层（Exchange）
	封装请求响应模式，同步转异步，以Request和Response为中心。
	扩展接口: Exchanger、ExchangeChannel、ExchangeClient和ExchangeServer。

网络传输层（Transport）
	抽象mina和netty为统一接口，以Message为中心。
	扩展接口: Channel、Transporter、Client、Server和Codec。

数据序列化层（Serialize）
	扩展接口: Serialization、ObjectInput、ObjectOutput和ThreadPool。