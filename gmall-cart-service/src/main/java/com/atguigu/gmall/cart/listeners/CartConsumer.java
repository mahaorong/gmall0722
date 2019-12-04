package com.atguigu.gmall.cart.listeners;

import com.atguigu.gmall.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class CartConsumer {

    @Autowired
    CartService cartService;

    @JmsListener(containerFactory = "jmsQueueListener", destination = "CART_LOGIN")
    public void consumerCartLogin(MapMessage mapMessage) throws JMSException {
        String memberId = mapMessage.getString("memberId");
        String cartListStr = mapMessage.getString("cartListStr");
        cartService.mergCart(memberId, cartListStr);
        // 同步购物车数据
        // 查询数据库
        // 合并购物车数据
        // 插入数据库
        // 同步缓存
    }
}
