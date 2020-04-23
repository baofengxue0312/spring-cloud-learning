# 项目概览
![Cloud 升级迭代](https://tva1.sinaimg.cn/large/007S8ZIlly1ge3hl72apwj31gr0u044b.jpg)

- 尚硅谷《SpringCloud第二季-周阳》学习笔记

- 2020-04-22-至今：迅速熟悉相关基础理论

# 基本服务模块
`cloud-provider-payment8001`: 支付服务 
`cloud-consumer-order80`: 消费者订单服务

# 服务注册与发现
## Eureka

已停更，日后不再推荐使用。

## Zookeeper

注意点：

- 需要关闭 Linux 防火墙
- 版本一致性：maven jar 包依赖与服务器 Zookeeper 的版本应保持一致

## Consul

## Nacos 未进行
## 分布式系统三大特点 CAP
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

# 服务调用-Ribbon

## Ribbon 负载均衡

### Ribbon简介

`Spring Cloud Ribbon` 是基于 `Netflix Ribbon` 实现的一套 **客户端负载均衡的工具**。

主要功能是提供 **客户端的软件负载均衡算法和服务调用**。`Ribbon`客户端组件提供一系列完善的配置项如**连接超时、重试等**。简单来说就是在配置文件中列出 `Load Balancer` 后面所有的机器， `Ribbon` 会自动地帮助你基于某种规则(如**简单轮询，随机连接等**)去连接这些机器，我们很容易使用`Ribbon`实现自定义的负载均衡算法。

![Ribbon架构](https://idig8.com/wp-content/uploads/2019/06/11223715-a9ffadd03624f133-1.png)

### 负载均衡是指什么？

简单的说就是将用户的请求平摊地分配到多个服务上，从而达到系统的HA(High Availability)，常见的负载均衡软件有Nginx、LVS，硬件有F5等。

### Ribbon本地负载均衡客户端与Nginx服务端负载均衡区别

Nginx是服务器负载均衡，客户端所有请求都会教给Nginx，然后由Nginx实现转发请求。

Ribbon是本地负载均衡，在调用微服务接口时，会在注册中心获取注册信息服务列表之后缓存到JVM本地，从而在本地实现RPC(远程过程调用)远程服务调用。

### 进程内LB与集中式LB

进程内LB: 将LB逻辑集成到消费方，消费方从服务注册中心获知有哪些地址可用，然后自己再从这些地址中选择出一个合适的服务器。Ribbon就属于进程内LB，它只是一个类库，集成于消费方进程，消费方通过它来获取到服务提供方的地址。

集中式LB: 即在服务的消费方与提供方之间使用独立的LB设施(可以是硬件如F5，软件如Nginx)，由该设施负责把访问请求通过某种策略转发至服务的提供方。

### Ribbon负载均衡策略

- IRule接口

```java
package com.netflix.loadbalancer;

public interface IRule {
    Server choose(Object var1);

    void setLoadBalancer(ILoadBalancer var1);

    ILoadBalancer getLoadBalancer();
}
```

![IRule及其实现类](https://tva1.sinaimg.cn/large/007S8ZIlly1ge402db4n6j31le0mcab1.jpg)

|         算法名称          |                           作用方式                           |
| :-----------------------: | :----------------------------------------------------------: |
|      RoundRobinRule       |                             轮询                             |
| WeightedResponseTimeRule  | 对轮询的扩展，响应速度越快的实例选择权重越大，越容易被选择。 |
|        RandomRule         |                             随机                             |
|         RetryRule         | 先按照轮询的策略获取服务，如果获取服务失败则在指定时间内会进行重试。 |
|     BestAvailableRule     | 会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，然后选择一个并发量最小的服务 |
| AvailabilityFilteringRule |            先过滤掉故障实例，再选择并发较小的实例            |
|     ZoneAvoidanceRule     | 默认规则，符合判断Server所在区域的性能和Server的可用性选择服务器。 |

- 替换默认负载均衡策略
  - 与`SpringBootApplication`所在包同级包下新建包`MySelfRule`，配置`@Configuration、@Bean`等，返回需要的策略。不能与`SpringBootApplication`主启动类同级包下是因为`SpringBootApplication`是一个复合注解，包含了`@ComponentScan`注解，而`Ribbon`自定义策略如果在这个注解扫描下将会全局通用，失去了自制的目的。
  - 在主启动类上添加注解`@RibbonClient(name = "需要定制的服务名称",configuration = 自定义策略类.class)`

### Ribbon负载均衡算法详解

- [ ] 原理、源码、手写，待专题分析。

轮询：实际调用服务器位置下标=REST接口请求次数%服务集群总数量，每次服务重启后REST接口计数从1开始

```java
public Server choose(ILoadBalancer lb, Object key) {
    if (lb == null) {
        log.warn("no load balancer");
        return null;
    } else {
        Server server = null;
        int count = 0;

        while(true) {
            if (server == null && count++ < 10) {
                List<Server> reachableServers = lb.getReachableServers();
                List<Server> allServers = lb.getAllServers();
                int upCount = reachableServers.size();
                int serverCount = allServers.size();
                if (upCount != 0 && serverCount != 0) {
                    int nextServerIndex = this.incrementAndGetModulo(serverCount);
                    server = (Server)allServers.get(nextServerIndex);
                    if (server == null) {
                        Thread.yield();
                    } else {
                        if (server.isAlive() && server.isReadyToServe()) {
                            return server;
                        }

                        server = null;
                    }
                    continue;
                }

                log.warn("No up servers available from load balancer: " + lb);
                return null;
            }

            if (count >= 10) {
                log.warn("No available alive servers after 10 tries from load balancer: " + lb);
            }

            return server;
        }
    }
}

private int incrementAndGetModulo(int modulo) {
    int current;
    int next;
    do {
        current = this.nextServerCyclicCounter.get();
        next = (current + 1) % modulo;
    } while(!this.nextServerCyclicCounter.compareAndSet(current, next));

    return next;
}
```

# 服务调用-OpenFeign

[Spring Cloud OpenFeign 官方文档](https://cloud.spring.io/spring-cloud-openfeign/2.2.x/reference/html/#spring-cloud-feign)

## Feign是什么？

Feign是一个声明式WebService客户端。使用Feign能让编写WebService变得更加简单。它的使用方式是**定义一个服务接口**然后在上面**添加注解**。Feign也支持可插拔式的编码器和解码器。Spring Cloud对Feign进行了封装，使其支持Spring MVC标准注解和HttpMessageConverters。Feign可与Eureka和Ribbon组合使用支持负载均衡。

## Feign能干什么？

Feign旨在使用JavaHttp客户端变得更加容易。

以往使用`Ribbon+RestTemplate`，利用`RestTemplate`对HTTP请求的封装处理，形成了一套模板化的调用方法。但是在实际开发中，由于对服务依赖的调用可能不止一处，往往一个接口会被多处调用，所以通常会针对每个微服务自行封装一些客户端类库来包装这些依赖服务的调用。所以Feign在此基础上做了进一步封装，来帮助我们定义和实现依赖服务接口的定义。在Feign的实现下，通过注解即可完成对服务提供方的接口绑定。并且集成了`Ribbon`.

## Feign与OpenFeign

|   名称    |                             简介                             |
| :-------: | :----------------------------------------------------------: |
|   Feign   | SpringCloud组件中的一个轻量级RESTful的HTTP客户服务端，内置了Ribbon，用来做客户端负载均衡，去调用服务注册中心的服务。利用Feign的注解定义接口，调用这个接口，就可以调用服务注册中心的服务。 |
| OpenFeign | 在Feign的基础上支持了Spring MVC的注解。使用`@FeignClint`可以解析Spring MVC的`@RequestMapping`下的接口，并通过动态代理的方式产生实现类，实现类中做负载均衡并调用其他服务。 |

## OpenFeign超时控制

默认1s，超时报错，可通过 `application.yml` 修改

```yaml
# 设置feign 客户端超时时间（openFeign默认支持ribbon）
ribbon:
  # 指的是建立连接所用的时间，适用于网络状况正常的情况下，两端连接所用的时间
  ReadTimeout: 5000
  # 指的是建立连接后从服务器读取到可用资源所用的时间
  ConnectTimeout: 5000     
```

## OpenFeign日志打印功能

创建配置类

```java
@Configuration
public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
```

修改配置文件 `application.yml`

```yaml
logging:
  level:
    # feign日志以什么级别监控哪个接口
    edu.dlut.springcloud.service.PaymentFeignService: debug
```

