package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsOrder;

public interface OrderService {

    void addOrder(OmsOrder omsOrder);

    boolean checkTradeCode(String memberId, String tradeCode);

    String getTradeCode(String memberId);
}
