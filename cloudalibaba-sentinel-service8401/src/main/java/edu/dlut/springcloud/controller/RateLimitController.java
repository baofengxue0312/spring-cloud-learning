package edu.dlut.springcloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import edu.dlut.springcloud.entity.CommonResult;
import edu.dlut.springcloud.entity.Payment;
import edu.dlut.springcloud.handler.MyHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  13:27
 * DESCRIPTION:
 **/
@RestController
public class RateLimitController {

    @GetMapping("/byResource")
    @SentinelResource(value = "byResource", blockHandler = "handleException")
    public CommonResult<Payment> byResource() {
        return new CommonResult<>(200, "按照资源名称限流成功！", new Payment(200L, "serial001"));
    }

    public CommonResult handleException(BlockException exception) {
        return new CommonResult(444, exception.getClass().getCanonicalName() + "\t服务不可用！");
    }

    @GetMapping("/rateLimit/byUrl")
    @SentinelResource(value = "byUrl")
    public CommonResult<Payment> byUrl() {
        return new CommonResult<>(200, "按照Url限流成功！", new Payment(200L, "serial002"));
    }

    //CustomBlockHandler
    @RequestMapping(value = "/rateLimit/customBlock", method = RequestMethod.GET)
    @SentinelResource(value = "customBlock", blockHandlerClass = MyHandler.class, blockHandler = "handlerException2")
    public CommonResult<Payment> customBlockHandler() {
        return new CommonResult<>(200, "按照客户自定义限流成功！", new Payment(200L, "serial003"));
    }

}
