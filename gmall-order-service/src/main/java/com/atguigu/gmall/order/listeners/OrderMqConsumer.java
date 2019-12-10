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

    @JmsListener(containerFactory = "jmsQueueListener", destination = "SKU_DEDUCT_QUEUE")
    public void consumerSkuDeduct(MapMessage mapMessage) throws JMSException {
        // 获得外部订单号（订单号）
        String orderId = mapMessage.getString("orderId");

        // 更新订单信息，更新为商品已出库
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setId(orderId);
        omsOrder.setStatus("2");
        omsOrder.setDeliveryCompany("硅谷物流");
        omsOrder.setDeliverySn("111111");
        omsOrder.setDeliveryTime(new Date());

        orderService.updateOrderById(omsOrder);
    }

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
        orderService.sendOrderSuccessQueue(omsOrder);
    }
}
