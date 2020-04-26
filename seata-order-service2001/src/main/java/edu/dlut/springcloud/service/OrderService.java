package edu.dlut.springcloud.service;

import edu.dlut.springcloud.domain.Order;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  19:52
 * DESCRIPTION:
 **/
public interface OrderService {
    /**
     * 创建订单
     *
     * @param order
     */
    void create(Order order);
}
