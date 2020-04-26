package edu.dlut.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/25  19:35
 * DESCRIPTION:
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class Payment9002 {
    public static void main(String[] args) {
        SpringApplication.run(Payment9002.class);
    }
}
