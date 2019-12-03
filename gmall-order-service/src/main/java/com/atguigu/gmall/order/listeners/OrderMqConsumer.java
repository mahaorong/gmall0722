package com.atguigu.gmall.order.listeners;

import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;

@Component
public class OrderMqConsumer {

    @Autowired
    OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener", destination = "PAYMENT_SUCCESS_QUEUE")
    public void consumerPaymentSuccess(MapMessage mapMessage) throws JMSException {

        String out_trade_no = mapMessage.getString("out_trade_no");

        // 更新订单信息
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(out_trade_no);
        omsOrder.setStatus("1");
        omsOrder.setPayType(1);
        omsOrder.setPaymentTime(new Date());
        orderService.updateOrder(omsOrder);

        // 发出库存消息队列（锁定订单中涉及到的商品的库存）
        System.out.println(out_trade_no);
    }
}
