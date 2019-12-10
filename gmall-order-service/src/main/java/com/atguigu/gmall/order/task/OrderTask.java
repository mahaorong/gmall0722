package com.atguigu.gmall.order.task;

import org.springframework.stereotype.Component;

@Component
//@EnableScheduling
public class OrderTask {

    //@Scheduled(cron = "0/9 * * * * ?")
    public void work() {

        // 扫描数据库

        // 循环订单集合

        // 订单创建时间与当前时间进行对比

        // 删除已经超过24小时的订单

        System.out.println("扫描并删除过期订单");
    }
}
