package edu.dlut.springcloud.controller;

import edu.dlut.springcloud.domain.CommonResult;
import edu.dlut.springcloud.domain.Order;
import edu.dlut.springcloud.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  19:56
 * DESCRIPTION:
 **/
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 创建订单 http://localhost:2001/order/create?userId=1&productId=1&count=10&money=100
     *
     * @param order
     * @return
     */
    @GetMapping("/create")
    public CommonResult create(Order order) {
        orderService.create(order);
        return new CommonResult(200, "订单创建成功");
    }
}
