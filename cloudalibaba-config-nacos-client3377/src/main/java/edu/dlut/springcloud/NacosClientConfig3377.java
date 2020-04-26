package edu.dlut.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/25  22:33
 * DESCRIPTION:
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class NacosClientConfig3377 {
    public static void main(String[] args) {
        SpringApplication.run(NacosClientConfig3377.class);
    }
}
