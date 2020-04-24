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

