package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentInfoService;
import com.atguigu.gmall.utils.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn", paymentInfo.getOrderSn());
        paymentInfoMapper.updateByExample(paymentInfo, example);
    }

    @Override
    public void addPayment(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void sendPaymentSuccessQueue(PaymentInfo paymentInfo) {
        ConnectionFactory connect = activeMQUtil.getConnectionFactory();
        try {
            Connection connection = connect.createConnection();
            connection.start();
            Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");

            MessageProducer producer = session.createProducer(queue);

//            ActiveMQTextMessage message = new ActiveMQTextMessage();
//            message.setText("支付完成，修改订单信息");

            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no", paymentInfo.getOrderSn());
            mapMessage.setString("status", "success");

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPaymentSuccessCheckQueue(PaymentInfo paymentInfo, int count) {
        ConnectionFactory connect = activeMQUtil.getConnectionFactory();
        try {
            Connection connection = connect.createConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("PAYMENT_CHECK_QUEUE");

            MessageProducer producer = session.createProducer(queue);

            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no", paymentInfo.getOrderSn());
            mapMessage.setInt("count", count);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000 * 10); // 延迟10秒后

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);

            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public PaymentInfo checkPaymentStatus(PaymentInfo paymentInfo) {
        // 调用支付宝接口

        // 查询交易的支付状态
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        HashMap<String, Object> requestMap = new HashMap<>();

        requestMap.put("out_trade_no", paymentInfo.getOrderSn());
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (response.isSuccess()) {
            System.out.println(response.getTradeStatus());
            String tradeStatus = response.getTradeStatus();
            String tradeNo = response.getTradeNo();
            paymentInfo.setAlipayTradeNo(tradeNo);
            paymentInfo.setPaymentStatus(tradeStatus);
            System.out.println("用户已经登录或扫码，支付的付款方已经确定，交易已创建，调用成功");
        } else {
            System.out.println(response.getTradeStatus());
            System.out.println("用户未登录或扫码，支付的付款方未确定，交易未创建，调用失败");
        }

        return paymentInfo;
    }

    @Override
    public String checkPayStatus(String out_trade_no) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(out_trade_no);
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfo);
        return paymentInfo1.getPaymentStatus();
    }
}
