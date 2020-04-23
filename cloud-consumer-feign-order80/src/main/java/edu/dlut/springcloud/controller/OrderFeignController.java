package edu.dlut.springcloud.controller;

import edu.dlut.springcloud.entity.CommonResult;
import edu.dlut.springcloud.entity.Payment;
import edu.dlut.springcloud.service.PaymentFeignService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/23  22:55
 * DESCRIPTION:
 **/
@RestController
@RequestMapping("/consumer")
public class OrderFeignController {

    @Resource
    private PaymentFeignService paymentFeignService;

    @GetMapping("/payment/get/{id}")
    public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id) {
        return paymentFeignService.getPaymentById(id);
    }

    @GetMapping(value = "/payment/feign/timeout")
    public String paymentFeignTimeout(){
        //openfeign底层ribbon，客户端默认等待1秒钟
        return paymentFeignService.paymentFeignTimeout();
    }

}
