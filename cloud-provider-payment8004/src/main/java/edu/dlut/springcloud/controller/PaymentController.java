package edu.dlut.springcloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/23  08:54
 * DESCRIPTION:
 **/
@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    @Value("${server.port}")
    private String serverPort;

    @GetMapping(value = "/zk")
    public String paymentzk() {
        return "SpringCloud with zookeeper：" + serverPort + "\t" + UUID.randomUUID().toString();
    }
}
