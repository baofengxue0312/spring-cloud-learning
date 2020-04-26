package edu.dlut.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  13:34
 * DESCRIPTION:
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class NacosOrder84 {
    public static void main(String[] args) {
        SpringApplication.run(NacosOrder84.class);
    }
}
