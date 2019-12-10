package com.atguigu.gmall.repair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.atguigu.gmall.repair.mapper")
public class GmallRepairApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallRepairApplication.class, args);
    }

}
