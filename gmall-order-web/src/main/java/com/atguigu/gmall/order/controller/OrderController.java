package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class OrderController {

    @Reference
    SkuService skuService;

    @RequestMapping("/toTrade")
    @LoginRequired
    public String toTrade(HttpServletRequest request) {

        String memberId = (String) request.getAttribute("memberId");

        return "trade";
    }
}
