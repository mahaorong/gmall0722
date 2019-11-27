package com.atguigu.gmall.seckill;

import com.atguigu.gmall.utils.RedisUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;

@Controller
public class StockController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("/getStock")
    @ResponseBody
    public String getStock() {

        Jedis jedis = null;
        long restStock = 0;

        RLock rLock = redissonClient.getLock("a");
        rLock.lock();
        try {
            jedis = redisUtil.getJedis();

            BigDecimal stock = new BigDecimal(jedis.get("stock"));

            int i = stock.compareTo(new BigDecimal("0"));// -1 0 1
            if(i > 0) {

                restStock = jedis.incrBy("stock", -1);

                System.out.println("售出一件商品，剩余库存数量" + restStock);
            }else {
                restStock = 0;
                System.out.println("商品售罄");
            }

        }finally {
            rLock.unlock();
            jedis.close();
        }


        return "";
    }
}
