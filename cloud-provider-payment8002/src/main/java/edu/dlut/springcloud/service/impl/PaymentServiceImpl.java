package edu.dlut.springcloud.service.impl;

import edu.dlut.springcloud.dao.PaymentDAO;
import edu.dlut.springcloud.entity.Payment;
import edu.dlut.springcloud.service.PaymentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/22  16:32
 * DESCRIPTION:
 **/
@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentDAO paymentDAO;

    @Override
    public int create(Payment payment) {
        return paymentDAO.create(payment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentDAO.getPaymentById(id);
    }
}
