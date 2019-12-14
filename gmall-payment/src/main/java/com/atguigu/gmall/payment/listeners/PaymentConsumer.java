package com.atguigu.gmall.payment.listeners;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;

@Component
public class PaymentConsumer {

    @Autowired
    PaymentInfoService paymentInfoService;

    @JmsListener(containerFactory = "jmsQueueListener", destination = "PAYMENT_CHECK_QUEUE")
    public void consumPaymentCheck(MapMessage mapMessage) throws JMSException {
        PaymentInfo paymentInfo = new PaymentInfo();
        String out_trade_no = mapMessage.getString("out_trade_no");
        int count = mapMessage.getInt("count");
        paymentInfo.setOrderSn(out_trade_no);

        // 调用延迟检查
        // 检查成功则结束延迟检查队列，触发系统支付成功的分布式任务
        System.out.println("执行支付检查任务，剩余次数" + count + "次");
        PaymentInfo paymentInfoResult = paymentInfoService.checkPaymentStatus(paymentInfo);

        // 检查失败，发送下次检查的延迟任务
        if (paymentInfoResult.getPaymentStatus() != null && ("TRADE_SUCCESS".equals(paymentInfoResult.getPaymentStatus()) || "已支付".equals(paymentInfoResult.getPaymentStatus()))){
            // 支付成功
            System.out.println("已经支付成功，更新支付信息，发送支付成功队列。。。");
            // 支付状态的幂等性检查
            String payStatus = paymentInfoService.checkPayStatus(out_trade_no);
            if(!"已支付".equals(payStatus)) {
                // 调用支付系统，修改为已支付
                PaymentInfo paymentInfoUpdate = new PaymentInfo();
                paymentInfoUpdate.setPaymentStatus("已支付");
                paymentInfoUpdate.setCallbackTime(new Date());
                paymentInfoUpdate.setCallbackContent("检查成功");
                String trade_no = paymentInfoResult.getAlipayTradeNo();
                paymentInfoUpdate.setAlipayTradeNo(trade_no);
                paymentInfoUpdate.setOrderSn(out_trade_no);
                paymentInfoService.updatePaymentInfo(paymentInfoUpdate);

                // 发送消息：调用订单系统，修改为已支付
                paymentInfoService.sendPaymentSuccessQueue(paymentInfoUpdate);
            }
        }else {
            if (count > 0) {
                count --;
                paymentInfoService.sendPaymentSuccessCheckQueue(paymentInfo, count);
                System.out.println("执行支付检查任务，检查失败，继续发送检查，剩余次数 " + count + " 次");
            }else {
                System.out.println("剩余次数 " + count + " 次, 不再继续检查");
            }
        }
    }
}
