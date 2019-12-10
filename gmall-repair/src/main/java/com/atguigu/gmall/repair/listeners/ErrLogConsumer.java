package com.atguigu.gmall.repair.listeners;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.service.ErrLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.MapMessage;

@Component
public class ErrLogConsumer {

    @Autowired
    ErrLogService errLogService;

    @JmsListener(containerFactory = "", destination = "ActiveMQ.DLQ")
    public void consumDLQ(MapMessage mapMessage) {

        errLogService.addErrLog(JSON.toJSONString(mapMessage));

        System.out.println("短信，邮件，站内信，日志形式来处理异常服务，补偿性操作");

    }
}
