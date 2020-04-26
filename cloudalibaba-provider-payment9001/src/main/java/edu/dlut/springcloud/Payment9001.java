package edu.dlut.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/25  19:31
 * DESCRIPTION:
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class Payment9001 {
    public static void main(String[] args) {
        SpringApplication.run(Payment9001.class);
    }
}
