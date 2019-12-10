package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsOrder;

public interface OrderService {

    void addOrder(OmsOrder omsOrder);

    boolean checkTradeCode(String memberId, String tradeCode);

    String getTradeCode(String memberId);

    OmsOrder getOrderBySn(String orderSn);

    void updateOrder(OmsOrder omsOrder);

    void sendOrderSuccessQueue(OmsOrder omsOrder);

    void updateOrderById(OmsOrder omsOrder);
}