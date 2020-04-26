package edu.dlut.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/25  21:08
 * DESCRIPTION:
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class NacosOrder83 {
    public static void main(String[] args) {
        SpringApplication.run(NacosOrder83.class);
    }
}
