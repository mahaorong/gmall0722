package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentInfoService {

    void updatePaymentInfo(PaymentInfo paymentInfo);

    void addPayment(PaymentInfo paymentInfo);

    void sendPaymentSuccessQueue(PaymentInfo paymentInfo);

    void sendPaymentSuccessCheckQueue(PaymentInfo paymentInfo, int count);

    PaymentInfo checkPaymentStatus(PaymentInfo paymentInfo);

    String checkPayStatus(String out_trade_no);
}
