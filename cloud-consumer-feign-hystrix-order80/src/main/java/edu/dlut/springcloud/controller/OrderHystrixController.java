package edu.dlut.springcloud.controller;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import edu.dlut.springcloud.service.PaymentHystrixService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/24  09:30
 * DESCRIPTION:
 **/
@RestController
@Slf4j
@RequestMapping("/consumer")
@DefaultProperties(defaultFallback = "paymentTimeoutFallbackMethod")
public class OrderHystrixController {

    @Resource
    private PaymentHystrixService paymentHystrixService;

    @GetMapping("/payment/hystrix/ok/{id}")
    public String paymentInfo_OK(@PathVariable("id") Integer id) {
        return paymentHystrixService.paymentInfo_OK(id);
    }

    @GetMapping("/payment/hystrix/timeout/{id}")
//    @HystrixCommand(fallbackMethod = "paymentTimeoutFallbackMethod", commandProperties = {
//            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1500")
//    })
    @HystrixCommand
    public String paymentInfoTimeOut(@PathVariable("id") Integer id) {
        int a = 10 / 0;
        return paymentHystrixService.paymentInfo_TimeOut(id);
    }

    public String paymentTimeoutFallbackMethod() {
        return "Consumer-ServerPort:80. 支付系统繁忙，N秒钟后重试或运行出错进行自检";
    }

}
