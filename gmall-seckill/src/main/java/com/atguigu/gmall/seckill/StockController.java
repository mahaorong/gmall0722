package com.atguigu.gmall.seckill;

import com.atguigu.gmall.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
public class StockController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("/seckill/two")
    @ResponseBody
    public String seckill2() {
        String result = "success";

        RSemaphore semaphore = redissonClient.getSemaphore("sku:122:stock");
        boolean b = semaphore.tryAcquire(); // 信号灯
        if (b) {
            System.out.println("抢购成功，目前库存剩余数量...");
        } else {
            System.out.println("抢购失败，库存没有了");
        }

        return result;
    }

    @RequestMapping("/seckill")
    @ResponseBody
    public String seckill(String userId) {
        String result = "";
        String uuid = UUID.randomUUID().toString();
        // 访问数据库，缓存
        Jedis jedis = null;

        try {
            // 限制用户访问频率
            String OK = jedis.set("user:" + userId + "stockLock", uuid, "nx", "ex", 1000 * 60 * 30); // EX = seconds; PX = milliseconds

            if (StringUtils.isNotBlank(OK) && "OK".equals(OK)) {
                jedis = redisUtil.getJedis();
                jedis.watch("sku:122:stock");
                String stock = jedis.get("sku:122:stock");
                long lstock = Long.parseLong(stock);
                if (lstock > 0) {
                    Transaction multi = jedis.multi();
                    // 减库存
                    multi.incrBy("sku:122:stock", -1);// lstock --;
                    //jedis.set("sku:122:stock", lstock + "");
                    // exec是key的值
                    List<Object> exec = multi.exec();
                    if (exec != null && exec.size() > 0) {
                        System.out.println("抢购成功，库存数量为：" + exec.get(0));
                        result = exec.get(0) + "";
                        // 发送订单
                    } else {
                        // 如果用户抢购失败则释放锁
                        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                        jedis.eval(script, Collections.singletonList("sku:122:lock"), Collections.singletonList(uuid));
                        System.out.println("抢购失败，下手慢了，当前库存已经被抢完了");
                    }
                } else {
                    // 抢购成功，更新用户限制时间
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    jedis.eval(script, Collections.singletonList("sku:122:lock"), Collections.singletonList(uuid));
                    jedis.set("user:" + userId + "stockLock", uuid, "nx", "ex", 1000 * 60 * 30);
                    System.out.println("抢购失败，库存不足");
                }
            } else {
                System.out.println("你已经抢购过了");
            }
        } finally {
            jedis.close();
        }
        return result;
    }

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
            if (i > 0) {

                restStock = jedis.incrBy("stock", -1);

                System.out.println("售出一件商品，剩余库存数量" + restStock);
            } else {
                restStock = 0;
                System.out.println("商品售罄");
            }

        } finally {
            rLock.unlock();
            jedis.close();
        }


        return "";
    }
}
