package com.fqm.test.mq.controller;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListener;
import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.client.producer.SendResult;
import com.fqm.test.mq.model.User;

@RestController
public class RabbitMqController extends MqController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    @Value("${mq.mqs.b.binder:}")
    private String mqBinder;
    @Value("${mq.topic}")
    private String topic;

    @MqListener(name = "${mq.mqs.b.name}")
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---rabbit---1=" + message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }

    @MqListener(name = "${mq.mqs.b1.name}")
    public void receiveMessage2(String message) {
        logger.info("receiveMessage---rabbit---2=" + message);
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }

    @MqListener(name = "${mq.mqs.b-dead.name}")
    public void mqDLQ1(String message) {
        logger.info("rabbit.DLQ=" + message);
    }

    @GetMapping("/mq/rabbit/sendMessage")
    public Object sendRabbitMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(mqBinder).syncSend(topic, user);
            logger.info("rabbit.send->{}", flag);

            mqFactory.getMqTemplate(MqMode.rabbit).asyncSend(topic, user, new SendCallback() {
                public void onSuccess(SendResult sendResult) {
                    logger.info("SendResult success," + sendResult.getId());
                }

                public void onException(Throwable e) {
                    logger.info("SendResult onException," + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    @GetMapping("/mq/rabbit/sendDelayMessage")
    public Object sendRabbitDelayMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(MqMode.rabbit).syncDelaySend(topic, user, 5, TimeUnit.SECONDS);
            logger.info("rabbit.sendDelay->{}", flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
