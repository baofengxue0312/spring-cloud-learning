package edu.dlut.springcloud.service.impl;

import edu.dlut.springcloud.service.IMessageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

import java.util.UUID;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/25  14:37
 * DESCRIPTION:
 **/
@EnableBinding(Source.class) // 定义消息的推送管道
public class IMessageProviderImpl implements IMessageProvider {

    @Qualifier("output")
    @Autowired
    private MessageChannel messageChannel;

    @Override
    public String send() {
        String serialNumber = UUID.randomUUID().toString();
        messageChannel.send(MessageBuilder.withPayload(serialNumber).build());
        System.out.println("---SerialNumber---: " + serialNumber);
        return serialNumber;
    }
}
