package com.atguigu.gmall.payment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class Boss_producer {

    public static void main(String[] args) {
        topic();
    }

    public static void topic() {

        ActiveMQConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");

        try {
            Connection connection = connect.createConnection();
            connection.start();
            Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            Topic meetting = session.createTopic("meetting");

            MessageProducer producer = session.createProducer(meetting);

            TextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText("为了尚硅谷的复兴而努力");

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void queue() {

        ActiveMQConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            // 第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("drink");

            MessageProducer producer = session.createProducer(testqueue);// 消息制造者

            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();// 文本消息
            textMessage.setText("我口渴了，请帮我倒一杯水。。。");// 消息内容

            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("k", "v");// hash格式的消息

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();// 事务
            connection.close();
        }catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

