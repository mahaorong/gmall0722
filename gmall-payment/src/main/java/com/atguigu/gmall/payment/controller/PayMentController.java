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
import com.atguigu.gmall.payment.util.HttpClient;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentInfoService;
import com.github.wxpay.sdk.WXPayUtil;
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

    @ResponseBody
    @RequestMapping("wx/submit")
    @LoginRequired
    public Map wxSubmit(String outTradeNo) {

        // 做一个判断：支付日志中的订单支付状态 如果是已支付，则不生成二维码直接重定向到消息提示页面！
        // 调用服务层数据
        if(outTradeNo.length()>32){
            outTradeNo = outTradeNo.substring(0,32);
        }

        // 第一个参数是订单Id ，第二个参数是多少钱，单位是分
        Map map = createNative(outTradeNo + "", "1");
        System.out.println(map.get("code_url"));
        // data = map
        return map;
    }

    private Map createNative(String outTradeNo, String money) {
        // 服务号Id
        String appid = "wxf913bfa3a2c7eeeb";
        // 商户号Id
        String partner = "1543338551";
        // 密钥
        String partnerkey = "atguigu3b0kn9g5v426MKfHQH7X8rKwb";
        //1.创建参数
        Map<String, String> param = new HashMap();//创建参数
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "尚硅谷");//商品描述
        param.put("out_trade_no", outTradeNo);//商户订单号
        param.put("total_fee", money);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://2z72m78296.wicp.vip/wx/callback/notify");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型
        try {
            //2.生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获得结果
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, String> map = new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", money);//总金额
            map.put("out_trade_no", outTradeNo);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @LoginRequired
    @RequestMapping("/alipay/callback/return")
    public String callbackReturn(HttpServletRequest request) {

        String sign = request.getParameter("sign");
        String out_trade_no = request.getParameter("out_trade_no");

        String payStatus = paymentInfoService.checkPayStatus(out_trade_no);
        if(!"已支付".equals(payStatus)) {
            // 调用支付系统，修改为已支付
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setCallbackContent(request.getQueryString());
            String trade_no = request.getParameter("trade_no");
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfoService.updatePaymentInfo(paymentInfo);

            // 发送延迟消息：检查是否修改为已支付
            paymentInfoService.sendPaymentSuccessQueue(paymentInfo);// 并行程序，异步
        }


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

        // 启动延迟任务，定时检查支付结果
        paymentInfoService.sendPaymentSuccessCheckQueue(paymentInfo, 7);

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
