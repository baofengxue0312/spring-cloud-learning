package edu.dlut.springcloud.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/22  17:17
 * DESCRIPTION:
 **/
@Configuration
public class ApplicationContextConfig {

    @Bean
    // @LoadBalanced
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}
