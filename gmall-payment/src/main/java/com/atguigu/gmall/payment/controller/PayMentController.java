package com.atguigu.gmall.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PayMentController {

    @RequestMapping("/index")
    public String index(String orderSn, String totalAmount, ModelMap modelMap) {

        modelMap.put("orderSn", orderSn);
        modelMap.put("totalAmount", totalAmount);
        return "index";
    }
}
