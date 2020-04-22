package edu.dlut.springcloud.dao;

import edu.dlut.springcloud.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/22  16:17
 * DESCRIPTION:
 **/
@Mapper
public interface PaymentDAO {

    int create(Payment payment);

    Payment getPaymentById(@Param("id") Long id);

}
