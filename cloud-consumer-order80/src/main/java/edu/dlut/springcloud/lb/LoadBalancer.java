package edu.dlut.springcloud.lb;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/23  21:50
 * DESCRIPTION:
 **/
public interface LoadBalancer {

    ServiceInstance instances(List<ServiceInstance> serviceInstances);

}
