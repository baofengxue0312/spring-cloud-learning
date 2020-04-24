package edu.dlut.springcloud.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import edu.dlut.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/24  08:47
 * DESCRIPTION:
 **/
@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping(value = "/hystrix/ok/{id}")
    public String paymentInfo_OK(@PathVariable("id") Integer id) {
        String result = paymentService.paymentInfo_OK(id);
        log.info("*****result：" + result);
        return result;
    }

    @GetMapping(value = "/hystrix/timeout/{id}")
    public String paymentInfo_TimeOut(@PathVariable("id") Integer id) {
        String result = paymentService.paymentInfo_Timeout(id);
        log.info("*****result：" + result);
        return result;
    }

    /**
     * 服务熔断
     * http://localhost:8001/payment/circuit/1
     *
     * @param id
     * @return
     */
    @GetMapping("/circuit/{id}")
    @HystrixCommand
    public String paymentCircuitBreaker(@PathVariable("id") Integer id) {
        String result = paymentService.paymentCircuitBreaker(id);
        log.info("***result:" + result);
        return result;
    }

}
