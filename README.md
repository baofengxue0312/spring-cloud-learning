# 项目概览

![Cloud 升级迭代](https://tva1.sinaimg.cn/large/007S8ZIlly1ge3hl72apwj31gr0u044b.jpg)

- 尚硅谷《SpringCloud第二季-周阳》学习笔记
- 2020-04-22-至今：迅速熟悉相关基础理论
- 在迅速熟悉相关技术理论后将分块整理各部分的使用方式，具体实现。

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

# 服务调用-Ribbon篇

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

# 服务调用-OpenFeign篇

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

# 服务降级-Hystrix篇

## 概述

[Hystrix GitHub官方文档](https://github.com/Netflix/Hystrix/wiki)

Hystrix是Netflix开源的一款容错框架，包含常用的容错方法：线程池隔离、信号量隔离、熔断、降级回退。在高并发访问下，系统所依赖的服务的稳定性对系统的影响非常大，依赖有很多不可控的因素，比如网络连接变慢，资源突然繁忙，暂时不可用，服务脱机等。

## Hystrix重要概念

[服务雪崩、降级、熔断-知乎](https://zhuanlan.zhihu.com/p/59109569)

|   名称   |                             描述                             |                          发生条件                           |
| :------: | :----------------------------------------------------------: | :---------------------------------------------------------: |
| 服务降级 | 服务器忙，请稍后再试，不让客户端等待立即返回一个友好提示,fallback | 程序运行异常、超时、服务熔断触发服务降级、线程池/信号量已满 |
| 服务熔断 | 类比保险丝达到最大服务访问后，直接拒绝访问，拉闸限电，然后调用服务降级的方法并返回友好提示 |             服务的降级->进而熔断->恢复调用链路              |
| 服务限流 | 秒杀高并发等操作，严禁一窝蜂的过来拥挤，大家排队，一秒钟N个。有序进行 |                                                             |

## Hystrix工作流程

[Hystrix技术解析-简书](https://www.jianshu.com/p/3e11ac385c73)

### JMeter压测

```shell
# mac 通过 Homebrew 安装 JMeter
$ brew install jmeter
$ brew install jmeter --with-plugins # 可选
$ jmeter 启动GUI
# 线程组->Add->Sampler->HTTP Request
```

- 异常

```
com.netflix.client.ClientException: Load balancer does not have available server for client
```

```yaml
eureka:
  client:
    fetch-registry: false # 将 false 改为 true 默认为 true
```

### 服务降级

当前服务不可用了，使用服务降级，调用兜底方案。既可以放在消费端，也可以放在服务端，但是一般放在消费端。

- 服务端

```java
// 处理方法注解
@HystrixCommand(fallbackMethod = "备用处理的方法[需要自己手写]", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")
    })
```

```java
// 主启动类注解
@EnableCircuitBreaker
```

- 消费端

```yaml
# yaml 文件
feign:
  hystrix:
    enabled: true
```

```java
// 主启动类
@EnableHystrix
// 方法配置使用 @HystrixCommand 注解
```

以上方法每个业务方法都存在一个兜底方法，代码冗余，要将统一的和自定义的进行分开隔离。

- 代码冗余解决方法

```java
// 在方法上加 @HystrixCommand 注解
@HystrixCommand
public String paymentInfoTimeOut(@PathVariable("id") Integer id) {
    int a = 10 / 0;
    return paymentHystrixService.paymentInfo_TimeOut(id);
}

// 在类上加 @DefaultProperties(defaultFallback = "paymentTimeoutFallbackMethod") 注解
// 需要自定义 fallback 方法
```

- 代码耦合度高解决办法

为 `Feign` 客户端定义的接口添加一个服务降级处理的实现类即可实现解耦。

可能要面对的异常：运行时异常，请求超时，服务器宕机。

```java
// 实现类上加 @Component 注解
// 接口上加
@FeignClient(value = "微服务名称", fallback = 实现类名称.class)
```

### 服务熔断

| 熔断类型 |                             描述                             |
| :------: | :----------------------------------------------------------: |
| 熔断打开 | 请求不再调用当前服务,内部设置一般为MTTR(平均故障处理时间),当打开长达导所设时钟则进入半熔断状态 |
| 熔断关闭 |                 熔断关闭后不会对服务进行熔断                 |
| 熔断半开 | 部分请求根据规则调用当前服务,如果请求成功且符合规则则认为当前服务恢复正常,关闭熔断 |

![img](https://martinfowler.com/bliki/images/circuitBreaker/state.png)

[Circuit Breaker - Martin Fowler](https://martinfowler.com/bliki/CircuitBreaker.html)

断路->自检->重试->恢复

```java
// Service
@HystrixCommand(fallbackMethod = "paymentCircuitBreaker_fallback", commandProperties = {
    @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),// 是否开启断路器
    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),// 请求次数
    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),// 时间窗口期/时间范文
    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60")// 失败率达到多少后跳闸
})
public String paymentCircuitBreaker(@PathVariable("id") Integer id) {
    if (id < 0)
        throw new IllegalArgumentException("*****id不能是负数");
    String serialNumber = IdUtil.simpleUUID();
    return Thread.currentThread().getName() + "\t" + "调用成功,流水号:" + serialNumber;
}

public String paymentCircuitBreaker_fallback(@PathVariable("id") Integer id) {
    return "id 不能负数,请稍后重试,o(╥﹏╥)o id:" + id;
}
// Controller
@GetMapping("/circuit/{id}")
@HystrixCommand
public String paymentCircuitBreaker(@PathVariable("id") Integer id) {
    String result = paymentService.paymentCircuitBreaker(id);
    log.info("***result:" + result);
    return result;
}
```

### 服务限流-推迟到Sentinel部分

### Hystrix工作流程

[How It Works - Hystrix Wiki](https://github.com/Netflix/Hystrix/wiki/How-it-Works)

![img](https://raw.githubusercontent.com/wiki/Netflix/Hystrix/images/hystrix-command-flow-chart.png)

## 服务监控HystrixDashboard

```java
// 添加相关依赖
// 配置 yml
// 主启动类上添加注解
@EnableHystrixDashboard
```

```java
// http://localhost:8001/hystrix.stream


// 在被监控的微服务的主启动类里添加
/**
* 此配置是为了服务监控而配置，与服务容错本身无观，springCloud 升级之后的坑
* ServletRegistrationBean因为springboot的默认路径不是/hystrix.stream
* 只要在自己的项目中配置上下面的servlet即可
*
* @return
*/
@Bean
public ServletRegistrationBean getServlet() {
    HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
    ServletRegistrationBean<HystrixMetricsStreamServlet> registrationBean = new ServletRegistrationBean<>(streamServlet);
    registrationBean.setLoadOnStartup(1);
    registrationBean.addUrlMappings("/hystrix.stream");
    registrationBean.setName("HystrixMetricsStreamServlet");
    return registrationBean;
}
```

# 服务网关-Gateway篇

## 概述

三大核心概念：路由，断言，过滤器。

## Spring Cloud Gateway 特性

- 动态路由: 能够匹配任何请求属性
- 可以对路由指定 Predicate(断言) 和 Filter(过滤器)
- 基成Hystrix断路器功能
- 集成Spring Cloud服务发现功能
- 易于编写的Predicate和Filter
- 请求限流功能
- 路径重写

## 路由跳转

### `yaml` 配置式

```yaml
spring:
  application:
    name: cloud-gateway
  cloud:
    gateway:
      routes:
        - id: payment_route # 路由的id,没有规定规则但要求唯一,建议配合服务名
          uri: http://localhost:8001
          # uri: lb://cloud-payment-service # 匹配后提供服务的路由地址
          predicates:
            - Path=/payment/get/** # 断言 路径相匹配的进行路由
        - id: payment_route2
          uri: http://localhost:8001
          # uri: lb://cloud-payment-service
          predicates:
            - Path=/payment/lb/** #断言,路径相匹配的进行路由
```

### 硬编码式

```java
@Configuration
public class Gateway {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        RouteLocatorBuilder.Builder routes = routeLocatorBuilder.routes();
        // 仍然是三个参数 "custom_path" 是id
        // route -> route.path("/guonei") 是 Predicate
        // uri("http://news.baidu.com/guonei") 是 uri
        routes.route("custom_path",
                     route -> route.path("/guonei")
                     .uri("http://news.baidu.com/guonei")).build();
        return routes.build();
    }
}
```

## 动态路由

```yaml
# 添加
spring:
  application:
    name: cloud-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
      routes:
        - id: payment_route # 路由的id 没有规定规则但要求唯一,建议配合服务名
          # uri: http://localhost:8001
          uri: lb://cloud-payment-service # lb://serviceName 是SpringCloud提供的负载均衡
          predicates:
            - Path=/payment/get/** # 断言 路径相匹配的进行路由
```

## Predicate

[使用方式 - Spring Cloud Gateway Predicate](https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.2.RELEASE/reference/html/#gateway-request-predicates-factories)

![网关启动控制台输出信息](https://tva1.sinaimg.cn/large/007S8ZIlly1ge54cudfkbj31fk0n6jy8.jpg)

![RoutePredicateFactory](https://tva1.sinaimg.cn/large/007S8ZIlly1ge54k9pmpuj33oe0kojte.jpg)

## Filter

[使用方式 - Spring Cloud Gateway Filter](https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.2.RELEASE/reference/html/#gatewayfilter-factories)

### 自定义 Filter

实现 `GlobalFilter, Ordered` 接口并重写 `filter` 方法

```java
@Component
@Slf4j
public class MyGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("come in global filter: {}", new Date());
        ServerHttpRequest request = exchange.getRequest();
        String uname = request.getQueryParams().getFirst("uname");
        if (uname == null) {
            log.info("用户名为null，非法用户");
            exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            return exchange.getResponse().setComplete();
        }
        // 放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
```

# 分布式配置中心

## 概述

[Spring Cloud Config - 官网](https://cloud.spring.io/spring-cloud-config/reference/html/)

### 是什么

每一个微服务都带着一个自己的 `application.yml`，如果项目里有几百个微服务配置文件会出现什么问题呢？

Spring Cloud Config 为微服务架构中的微服务提供集中化的外部配置支持，配置服务器为各个不同微服务应用的所有环境提供了一个中心化的外部支持。

### 能干什么

- 集中管理配置文件
- 不同环境不同配置，动态化的配置更新，分环境部署。
- 运行期间动态调整配置，不再需要在每个服务部署的机器上编写配置文件
- 当配置发生变动时，服务不需要重启即可感知到配置的变化并应用新的配置。
- 将配置信息以REST接口的形式暴露

## 服务端与客户端

### 服务端

```yaml
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```

```yaml
spring:
  application:
    name: cloud-config-center
  cloud:
    config:
      server:
        git:
          skipSslValidation: true # 跳过ssl认证
          uri: https://github.com/raymond-zhao/spring-cloud-config.git
          search-paths:
            - spring-cloud-config
      label: master
```

```java
// 主启动类
@EnableConfigServer
```

### 客户端

```yaml
# bootstrap.yml
```

## 动态刷新之手动版

在客户端 3355 微服务中修改

```xml
<!-- 添加 actuator 依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
# 暴露监控端点
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

```java
// Controller 添加
@RefreshScope
```

做完上述步骤之后，客户端还是不会自动刷新，需要手动发起 `POST` 请求

```shell
$ curl -X POST "http://localhost:3355/actuator/refresh"
```

假如有多个微服务客户端 3355、3366、3377...每个服务都要执行一次 `POST` 请求吗？能不能采用 **广播** 的形式？达到一次通知，处处生效的目的？

但是如果实现了广播，那假如有100个微服务，只修改了一个配置文件，其它的99个也要接收通知吗？

# 消息总线-Bus篇

## 概述

### 什么是总线？

在微服务架构的系统中，通常会使用 **轻量级的消息代理** 来构建一个 **共用的消息主题**，并让系统中所有的微服务实例都连接上来。由于**该主题中产生的消息会被所有实例监听和消费，所以称为消息总线**。在总线上的各个实例，都可以方便地广播一些需要让其他连接在该主题上的实例都知道的消息。

### 基本原理

`ConfigClient`实例都监听`MQ`中同一个`Topic(Spring Cloud Bus)`。当一个服务刷新数据的时候，它会把这个信息放入到 `Topic` 中，这样其它监听同一`Topic`的服务就能得到通知，然后去更新自身的配置。

### 消息代理种类

- Bus 支持两种消息代理: `RabbitMQ`和`Kafka`

## Bus动态刷新全局广播的设计思想和选型

- 利用消息总线触发一个客户端`/bus/refresh`，从而刷新所有客户端的配置。
- 利用消息总线触发一个服务端 `ConfigServer` 的 `/bus/refresh`端点，从而刷新所有客户端的配置。
- 应该选择第二种作为技术选型。

3344 的改动

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

```yaml
rabbitmq:
  host: localhost
  port: 5672
  username: guest
  password: guest
  
# 暴露bus刷新配置的端点
management:
  endpoints:
    web:
      exposure:
        include: "bus-refresh"
```

一次请求，处处生效。

```shell
$ curl -X POST "http://localhost:3344/actuator/bus-refresh"
```

## Bus定点通知

```shell
$ curl -X POST "http://localhost:配置中心的端口号/actuator/bus-refresh/{destination}"
# /bus/refresh 请求不再发送到具体的服务实例上，而是发给 Config Server 并通过 destination 参数类指定需要更新配置的服务或实例
# destination 是指项目 yaml 配置文件中的 spring.application.name:server.port
```

```shell
# 只通知 3355
$ curl -X POST "http://localhost:3344/actuator/bus-refresh/config-client:3355"
```

# 消息驱动-Stream篇

## 概述

[Spring Cloud Stream Documentation](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/3.0.4.RELEASE/reference/html/index.html)

### 是什么

屏蔽底层消息中间件的差异，降低切换成本，统一消息的编程模型。

### Spring Cloud Stream

Spring Cloud Stream 是一个构建消息驱动微服务的框架。

- 应用程序通过 `inputs` 或者 `outputs` 来与 Spring Cloud Stream 中`binder`对象交互。
- 通过配置来`binding`，而 Spring Cloud Stream 的`binder`对象负责与消息中间件交互。
- 通过使用`Spring Integration`来连接消息代理中间件以实现消息事件驱动。
- Spring Cloud Stream 为一些供应商的消息中间件产品提供了个性化的自动化配置实现，引用了发布-订阅、消费组、分区的三个核心概念。
- 目前(2020-04-25)仅支持`RabbitMQ,Kafka`

### 标准MQ

- 生产者-消费者之间靠 **消息(Message)** 媒介传递信息内容
- 消息必须走特定的**通道(Channel)**
- **消息通道(MeeageChannel)**的子接口`SubscribableChannel`，由`MessageHandler`消息处理器所订阅

### Binder

- 通过定义Binder作为中间层，完美地实现了 **应用程序与消息中间件细节之间的隔离**
- 通过向应用程序暴露统一的`Channel`通道，使得应用程序不需要再考虑各种不同的消息中间件实现

### Application Model与API

![SCSt with binder](https://raw.githubusercontent.com/spring-cloud/spring-cloud-stream/master/docs/src/main/asciidoc/images/SCSt-with-binder.png)



|       组成        |                                                         说明 |
| :---------------: | -----------------------------------------------------------: |
|   `Middleware`    |                            中间件，目前只支持RabbitMQ和Kafka |
|     `Binder`      | Binder是应用于消息中间件之间的封装，目前实行了Kafka和RabbitMQ的Binder，通过Binder可以很方便的连接中间件，可以动态的改变消息类型（对应于Kafka的topic，RabbitMQ的exchange），这些都可以通过配置文件来实现 |
|     `@Input`      |     注解标识输入通道，通过该输入通道接收到的消息进入应用程序 |
|     `@Output`     |         注解标识输出通道，发布的消息将通过该通道离开应用程序 |
| `@StreamListener` |                         监听队列，用于消费者的队列的消息接收 |
| `@EnableBinding`  |                            指信道channel和exchange绑定在一起 |

### Programming Model

![SCSt overview](https://raw.githubusercontent.com/spring-cloud/spring-cloud-stream/master/docs/src/main/asciidoc/images/SCSt-overview.png)

![SpringCloud Stream--编码API和常用注解- 柚子社区](https://uzshare.com/_p?https://img-blog.csdnimg.cn/20200320114343995.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NvbGRfX19wbGF5,size_16,color_FFFFFF,t_70)

- Binder：方便连接中间件，屏蔽差异
- Channel：通道，是队列Queue的一种抽象，在消息通讯系统中就是实现存储和转发的媒介，通过Channel对队列进行配置。
- Source、Sink：简单的可理解为参照对象是SpringCloud Stream自身，从Stream发布消息就是输出，接受消息就是输入。

## 消息驱动之生产者

`cloud-stream-rabbitmq-provider8801`

## 消息驱动之消费者

`cloud-eureka-server7001`

`cloud-stream-rabbitmq-provider8801`

`cloud-stream-rabbitmq-consumer8802`

## 分组消费与持久化

`cloud-eureka-server7001`

`cloud-stream-rabbitmq-provider8801`

`cloud-stream-rabbitmq-consumer8802`

`cloud-stream-rabbitmq-consumer8803`

### 重复消费

两个问题：重复消费，消息持久化。

设想一个场景，订单系统做集群部署，都会从`RabbitMQ`中获取订单信息，加入一个订单被两个服务获取到，那么一个人购买了一个东西，但是却产生了两条购买记录，对买家来说是好事，但是对卖家来说...

- 重复消费原因：默认分组 `group` 是不同的，组流水号不一样，被认为不同组，可以消费。

这时可以使用 `Spring Cloud Stream` 的消息分组来解决，在`Stream`中处于同一个`group`中的多个消费者是竞争关系，就能够保证消息只会被其中一个服务消费一次。

只需要在`application.yml`中修改配置即可，即`spring.cloud.stream.bindings.input.group` 设置同一个名称。

### 持久化

如果消费者机器下线了，但是生产者在消费者下线期间又生产了消息，那消费者及其上线后会消费在下线期间产生的消息。

# 分布式请求链路追踪-Sleuth篇

## 概述

[Spring Cloud Sleuth Documentation](https://cloud.spring.io/spring-cloud-static/spring-cloud-sleuth/2.2.2.RELEASE/reference/html/#introduction)

### 背景

在微服务框架中，一个由客户端发起的请求在后端系统中会经过多个不同的服务节点来协同产生最后的请求结果，每一个前端请求都会形成一条复杂的分布式服务调用链路，链路中的任何一环出现高延时或者错误都会引起整个请求的失败。

```xml
<!--包含了sleuth+zipkin-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

## 追踪

`cloud-eureka-server7001`

`cloud-eureka-server7002`

`cloud-provider-payment8001`

`cloud-consumer-order80`

```shell
# java -jar zipkin-server...jar
# localhost:9411/zipkin
```

# Spring Cloud Alibaba

## 概述

[Spring Cloud Alibaba - GitHub](https://github.com/alibaba/spring-cloud-alibaba)

[Spring Cloud Alibaba - Documentation](https://spring.io/projects/spring-cloud-alibaba)

## Nacos

[Nacos - 官方中文文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)

***在mac或者其他类Unix系统上启动nacos时不会再出现 Nacos 的小图案了！！！***

```shell
/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home/bin/java  -Xms512m -Xmx512m -Xmn256m -Dnacos.standalone=true -Djava.ext.dirs=/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home/jre/lib/ext:/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home/lib/ext -Xloggc:/Users/raymond/Downloads/nacos/logs/nacos_gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M -Dloader.path=/Users/raymond/Downloads/nacos/plugins/health,/Users/raymond/Downloads/nacos/plugins/cmdb,/Users/raymond/Downloads/nacos/plugins/mysql -Dnacos.home=/Users/raymond/Downloads/nacos -jar /Users/raymond/Downloads/nacos/target/nacos-server.jar  --spring.config.location=classpath:/,classpath:/config/,file:./,file:./config/,file:/Users/raymond/Downloads/nacos/conf/ --logging.config=/Users/raymond/Downloads/nacos/conf/nacos-logback.xml --server.max-http-header-size=524288
nacos is starting with standalone
nacos is starting，you can check the /Users/raymond/Downloads/nacos/logs/start.out
```

***切记几种启动方式的区别，默认开启是集群模式，根据情况使用。***

```shell
# localhost:8848/nacos
# 默认账号密码都是 nacos
$ sh startup.sh -m standalone
$ sh shutdown.sh
```

## Nacos服务注册

内置` Ribbon `负载均衡，需要结合`RestTemplate`使用，以及 `@LoadBalanced`注解

`cloudalibaba-provider-payment9001`

`cloudalibaba-provider-payment9002`

`cloudalibaba-consumer-nacos-order83`

[Nacos与其他注册中心对比 - 阿里云](https://developer.aliyun.com/article/738413)

***NACOS支持 AP 和 CP 的切换***

![2.png](https://ucc.alicdn.com/pic/developer-ecology/f22beabffa4646dab0497bfc6f2654f2.png)

## Nacos配置中心-基础配置

[Nacos Spring Cloud - 官方文档](https://nacos.io/zh-cn/docs/quick-start-spring-cloud.html)

`cloudalibaba-config-nacos-client3377`

新增 `bootstrap.yml, application.yml`

`nacos`配置管理中添加的配置文件后缀名只能是`yaml`，而不能是`yml`.

```yaml
# 配置文件名格式
${spring.application.name}-${spring.profile.active}.${spring.cloud.nacos.config.file-extension}
```

## Nacos配置中心-分类配置

### Namespace、Group、Data Id三者关系

![Namespace、Group、Data Id三者关系](https://tva1.sinaimg.cn/large/007S8ZIlly1ge6ettc0g7j30rq0ein04.jpg)

`Namespace`默认是`public`，`Namespace`主要用来实现隔离，假如有三个环境：开发、测试、生产，那么可以创建三个`Namespace`，不同的`Namespace`之间是相互隔离的。

`Group`默认是`Default_Group`,`Group`可以把不同的微服务划分到同一个分组

`Service`是微服务，一个`Service`可以包含多个`Cluster`，`Nacos Cluster`默认是`Default`，是对指定微服务的一个虚拟化分。

`Instance`是微服务的实例。

### Data Id配置

```yaml
# spring.profiles.active 即是 Data Id
spring:
  profiles:
    active: dev # 开发环境
#    active: test # 测试环境
#    active: info # 开发环境
```

### Group配置

```yaml
spring:
  cloud:
    nacos:
      config:
        group: DEV_GROUP
```

### Namespace配置

```yaml
spring:
  cloud:
    nacos:
      config:
        namespace: 11586938-ae5d-4332-b41a-603a3f37420a
```

## Nacos集群和持久化配置

[Nacos 集群 - 官方文档](https://nacos.io/zh-cn/docs/cluster-mode-quick-start.html)

### 持久化配置解释

[Nacos部署 - 官方文档](https://nacos.io/zh-cn/docs/deployment.html)

- `Nacos`默认自带的是嵌入式数据库`derby`
- `derby`到`MySQL`切换配置步骤
  - `nacos-server/nacos/conf`找到`nacos-sql`脚本，到`MySQL`数据库中执行。
  - 修改`conf/application.properties`文件，增加支持`MySQL`数据源配置(目前只支持`MySQL`)，添加`MySQL`数据源的相关信息。

- 重新启动`Nacos`，可以看到是个全新的空记录界面。

## Nacos集群配置

连接`MySQL`数据库通过`nginx`监听端口号`1111`，然后进行代理转发到集群的三台`nacos`服务器上。

- `/nacos/conf`找到`nacos-sql`脚本，到`MySQL`数据库中执行，生成数据库。
- 修改`conf/application.properties`文件，增加支持`MySQL`数据源配置(目前只支持`MySQL`)，添加`MySQL`数据源的相关信息。

```properties
spring.datasource.platform=mysql

db.num=1
db.url.0=jdbc:mysql://本机或服务器IP:3306/数据库名?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true
db.user=填写自己的
db.password=填写自己的
```

- 备份`conf/cluster.conf.example`到`conf/cluster.conf`用作修改文件

```shell
$ cp conf/cluster.conf.example conf/cluster.conf
```

- 修改`conf/cluster.conf`添加集群主机及端口号，建议填写本机局域网`ip`，而不要填`127.0.0.1`，`centOS`可通过`hostname -i`查看，`macOS`通过`ifconfig en0`查看。

```shell
$ vi cluster.conf
# It is ip
192.168.0.108:3333
192.168.0.108:4444
192.168.0.108:5555
```

- 备份并修改`bin/startup.sh`，增加 `-p 端口号`启动方式，默认启动方式不可传入`-p` 参数

```shell
$ vi startup.sh
# 进入 vim 之后按下 : 输入 set nu 显示行号 在差不多54行左右
# 在 57行 while getopts ":m:f:s:" 末尾添加 p: 表示端口的意思
# 添加 66、67 行
# 在134行左右处 ${JAVA} ${JAVA_OPT} 之间添加 -Dserver.port=${PORT} 表示接收端口参数
# :wq 保存退出
```

```sh
 54 export SERVER="nacos-server"
 55 export MODE="cluster"
 56 export FUNCTION_MODE="all"
 57 while getopts ":m:f:s:p:" opt
 58 do
 59     case $opt in
 60         m)
 61             MODE=$OPTARG;;
 62         f)
 63             FUNCTION_MODE=$OPTARG;;
 64         s)
 65             SERVER=$OPTARG;;
 66         p)
 67             PORT=$OPTARG;;
 68         ?)
 69         echo "Unknown parameter"
 70         exit 1;;
 71     esac
 72 done
```

```sh
134 nohup $JAVA -Dserver.port=${PORT} ${JAVA_OPT} nacos.nacos >> ${BASE_DIR}/log    s/start.out 2>&1 &
```

- 修改`nginx`配置

```shell
$ brew install nginx
$ vi /usr/local/etc/nginx/nginx.conf
# 添加 upstream cluster
# 修改监听端口
# 修改 location
```

```
 33     #gzip  on;
 34
 35     upstream cluster {
 36         server 127.0.0.1:3333;
 37         server 127.0.0.1:4444;
 38         server 127.0.0.1:5555;
 39     }
 40
 41     server {
 42         listen       1111;
 43         server_name  localhost;
 44
 45         #charset koi8-r;
 46
 47         #access_log  logs/host.access.log  main;
 48
 49         location / {
 50             #root   html;
 51             #index  index.html index.htm;
 52             proxy_pass http://cluster;
 53         }
 54    }
```

- 进入到`nacos/bin`，启动并验证

```shell
$ sh startup.sh -p 3333
$ sh startup.sh -p 4444
$ sh startup.sh -p 5555
$ ps -ef|grep nacos|grep -v grep|wc -l # 查看启动的nacos数量
$ cd /usr/local/Cellar/nginx/bin
$ ./nginx -c /usr/local/etc/nginx/nginx.conf
$ ps -ef | grep nginx # 查看 Nginx 启动情况
# 浏览器访问 localhost:1111/nacos/#/login
# 登录之后所进行的操作，比如添加配置项，启动微服务注册，将会被记录到 MySQL 数据库中。
```

