package edu.dlut.springcloud.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/24  19:52
 * DESCRIPTION:
 **/
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
