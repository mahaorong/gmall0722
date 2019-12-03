package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentInfoService {

    void updatePaymentInfo(PaymentInfo paymentInfo);

    void addPayment(PaymentInfo paymentInfo);

    void sendPaymentSuccessQueue(PaymentInfo paymentInfo);
}
