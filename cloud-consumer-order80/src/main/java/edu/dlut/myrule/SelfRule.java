package edu.dlut.myrule;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/23  21:21
 * DESCRIPTION:
 **/
@Configuration
public class SelfRule {
    @Bean
    public IRule getMyRule() {
        return new RandomRule();
    }
}
