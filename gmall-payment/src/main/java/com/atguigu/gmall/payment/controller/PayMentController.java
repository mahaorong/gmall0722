package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PayMentController {

    @Autowired
    AlipayClient alipayClient;
    
    @Autowired
    PaymentInfoService paymentInfoService;
    
    @Reference
    OrderService orderService;

    @LoginRequired
    @RequestMapping("/alipay/callback/return")
    public String callbackReturn(HttpServletRequest request, ModelMap modelMap) {

        String orderSn = (String)request.getAttribute("orderSn");

        // 调用支付系统，修改为已支付
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus("已支付");
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(request.getQueryString());
        String trade_no = request.getParameter("trade_no");
        paymentInfo.setAlipayTradeNo(trade_no);
        paymentInfo.setOrderSn(orderSn);
        paymentInfoService.updatePaymentInfo(paymentInfo);
        
        // 发送消息：调用订单系统，修改为已支付
        paymentInfoService.sendPaymentSuccessQueue(paymentInfo);

        // 调用库存系统，修改库存信息
        
        // 调用物流系统，生成物流信息
        
        return "finish";
    }

    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String index(String orderSn, BigDecimal totalAmount) {

        // 获取订单信息
        OmsOrder omsOrder = orderService.getOrderBySn(orderSn);
        
        //利用支付宝客户端生成表单页面
        AlipayTradePagePayRequest alipayRequest=new AlipayTradePagePayRequest();

        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        Map<String,String> paramMap=new HashMap<>();
        paramMap.put("out_trade_no",orderSn);
        paramMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        paramMap.put("total_amount","0.01");
        paramMap.put("subject","尚硅谷纪念版鸿蒙手机Gphone一部");

        String paramJson = JSON.toJSONString(paramMap);// 填充业务参数
        alipayRequest.setBizContent(paramJson);
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        System.out.println(form);

        // 生成支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(orderSn);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setSubject("尚硅谷鸿蒙系统纪念版Gphone手机一部");
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setCreateTime(new Date());
        paymentInfoService.addPayment(paymentInfo);
        
        return form;
    }

    @LoginRequired
    @RequestMapping("/index")
    public String index(String orderSn, String totalAmount, ModelMap modelMap) {

        modelMap.put("orderSn", orderSn);
        modelMap.put("totalAmount", totalAmount);
        return "index";
    }
}
