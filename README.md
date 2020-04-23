# spring-cloud-learning
![Cloud 升级迭代](https://tva1.sinaimg.cn/large/007S8ZIlly1ge3hl72apwj31gr0u044b.jpg)

尚硅谷《SpringCloud第二季-周阳》学习笔记

2020-04-22-至今：迅速熟悉相关基础理论

## 基本服务模块
`cloud-provider-payment8001`: 支付服务 
`cloud-consumer-order80`: 消费者订单服务

## 服务注册与发现
### Eureka

已停更，日后不再推荐使用。

### Zookeeper

注意点：

- 需要关闭 Linux 防火墙
- 版本一致性：maven jar 包依赖与服务器 Zookeeper 的版本应保持一致

### Consul

### Nacos 未进行
### 分布式系统三大特点 CAP
C: Consistency 强一致性
A: Availability 可用性
P: Partition Tolerance 分区容错性
CAP理论关注粒度是数据，而不是整体系统设计的策略，一般来说系统只能满足三者之其二。
AP：Eureka
CP：Zookeeper、Consul

|  组件名   | 语言 | CAP  | 服务健康检查 | 对外暴露接口 | SpringCloud集成 |
| :-------: | :--: | :--: | :----------: | :----------: | :-------------: |
|  Eureka   | Java |  AP  |   可配支持   |     HTTP     |     已集成      |
|  Consul   |  Go  |  CP  |     支持     |   HTTP/DNS   |     已集成      |
| Zookeeper | Java |  CP  |     支持     |    客户端    |     已集成      |

