package edu.dlut.springcloud.service;

import edu.dlut.springcloud.entity.CommonResult;
import edu.dlut.springcloud.entity.Payment;
import org.springframework.stereotype.Component;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  13:38
 * DESCRIPTION:
 **/
@Component
public class PaymentFallbackService implements PaymentService {

    @Override
    public CommonResult<Payment> paymentSQL(Long id) {
        return new CommonResult<>(444, "服务降级 ---- fallback");
    }

}