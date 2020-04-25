package edu.dlut.springcloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/25  12:32
 * DESCRIPTION:
 **/
@RestController
@RefreshScope
@Slf4j
@RequestMapping("/config")
public class ConfigClientController {

    @Value("${config.info}")
    private String configInfo;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/info")
    public String getConfigInfo() {
        return "serverPort: " + serverPort + " configInfo: " + configInfo;
    }

}
