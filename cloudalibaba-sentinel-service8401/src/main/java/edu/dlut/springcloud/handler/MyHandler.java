package edu.dlut.springcloud.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import edu.dlut.springcloud.entity.CommonResult;
import edu.dlut.springcloud.entity.Payment;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  13:29
 * DESCRIPTION:
 **/
public class MyHandler {

    /**
     * 注意一定要static
     *
     * @return
     */
    public static CommonResult<Payment> handlerException1(BlockException e) {
        return new CommonResult<>(444, "按照用户自定义1，全局的", new Payment(2020L, "serial004----1"));
    }

    public static CommonResult<Payment> handlerException2(BlockException e) {
        return new CommonResult<>(444, "按照用户自定义2，全局的", new Payment(2020L, "serial004----2"));
    }

}
