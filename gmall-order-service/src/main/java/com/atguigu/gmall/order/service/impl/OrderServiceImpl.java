package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.order.mapper.OmsOrderItemMapper;
import com.atguigu.gmall.order.mapper.OmsOrderMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.utils.ActiveMQUtil;
import com.atguigu.gmall.utils.RedisUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void addOrder(OmsOrder omsOrder) {

        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();

        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
        }
    }

    @Override
    public boolean checkTradeCode(String memberId, String tradeCode) {

        boolean b = false;
        Jedis jedis = redisUtil.getJedis();

        //对比防重删令牌
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

        Long eval = (Long) jedis.eval(script, Collections.singletonList("user:" + memberId + ":tradeCode"),
                Collections.singletonList(tradeCode));

        int i = new BigDecimal(eval).compareTo(new BigDecimal("0"));

        if (i != 0) {
            b=true;
        }
//        String uuid = jedis.get("user:" + memberId + ":tradeCode");
//
//        if (tradeCode.equals(uuid)) {
//            b = true;
//            jedis.del("user:" + memberId + ":tradeCode");
//        }
        jedis.close();

        return b;
    }

    @Override
    public String getTradeCode(String memberId) {

        String uuid = UUID.randomUUID().toString();

        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:" + memberId + ":tradeCode", 60 * 30, uuid);

        jedis.close();

        return uuid;
    }

    @Override
    public OmsOrder getOrderBySn(String orderSn) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(orderSn);
        return omsOrderMapper.selectOne(omsOrder);
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn", omsOrder.getOrderSn());
        omsOrderMapper.updateByExampleSelective(omsOrder, example);
    }

    @Override
    public void sendOrderSuccessQueue(OmsOrder omsOrder) {
        ConnectionFactory connect = activeMQUtil.getConnectionFactory();
        try {
            Connection connection = connect.createConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("ORDER_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(queue);

            // 封装消息队列中的订单对象
            OmsOrder omsOrderParam = new OmsOrder();
            omsOrderParam.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrderMessage = omsOrderMapper.selectOne(omsOrderParam);

            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setOrderSn(omsOrderParam.getOrderSn());
            List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.select(omsOrderItem);
            omsOrderMessage.setOmsOrderItems(omsOrderItems);

            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(JSON.toJSONString(omsOrderMessage));

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);

            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateOrderById(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("id", omsOrder.getId());

        omsOrderMapper.updateByExampleSelective(omsOrder, example);
    }
}
