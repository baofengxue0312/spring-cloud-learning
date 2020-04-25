package edu.dlut.springcloud.controller;

import edu.dlut.springcloud.service.IMessageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/25  14:41
 * DESCRIPTION:
 **/
@RestController
public class SendMessageController {

    @Autowired
    private IMessageProvider messageProvider;

    @GetMapping("/sendMessage")
    public String send() {
        return messageProvider.send();
    }

}
