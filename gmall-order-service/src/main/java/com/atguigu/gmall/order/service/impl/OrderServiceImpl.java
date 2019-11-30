package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.order.mapper.OmsOrderItemMapper;
import com.atguigu.gmall.order.mapper.OmsOrderMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

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
}
