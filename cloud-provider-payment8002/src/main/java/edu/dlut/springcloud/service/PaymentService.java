package edu.dlut.springcloud.service;

import edu.dlut.springcloud.entity.Payment;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/22  16:31
 * DESCRIPTION:
 **/
public interface PaymentService {

    int create(Payment payment);

    Payment getPaymentById(Long id);

}
