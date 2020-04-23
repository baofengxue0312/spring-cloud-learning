package edu.dlut.springcloud.lb;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/23  21:52
 * DESCRIPTION:
 **/
@Component
public class MyLoadBalancer implements LoadBalancer {

    private AtomicInteger nextServer = new AtomicInteger(0);

    private final int getAndIncrement() {
        int current;
        int next;
        do {
            current = this.nextServer.get();
            next = current >= Integer.MAX_VALUE ? 0 : current + 1;
        } while (!this.nextServer.compareAndSet(current, next));
        System.out.println("*****第几次访问, 次数 next：" + next);
        return next;
    }

    @Override
    public ServiceInstance instances(List<ServiceInstance> serviceInstances) {
        int index = getAndIncrement() % serviceInstances.size();
        return serviceInstances.get(index);
    }

}
