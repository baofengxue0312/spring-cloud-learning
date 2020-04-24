package edu.dlut.springcloud.service;

import org.springframework.stereotype.Component;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/24  13:42
 * DESCRIPTION:
 **/
@Component
public class PaymentFallBackService implements PaymentHystrixService {

    @Override
    public String paymentInfo_OK(Integer id) {
        return "PaymentFallbackService fall back--paymentInfo_OK";
    }

    @Override
    public String paymentInfo_TimeOut(Integer id) {
        return "PaymentFallbackService fall back--paymentInfo_TimeOut";
    }
}
