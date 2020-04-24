package edu.dlut.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/24  09:29
 * DESCRIPTION:
 **/
@SpringBootApplication
@EnableFeignClients
@EnableHystrix
public class OrderHystrix80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderHystrix80.class);
    }
}
