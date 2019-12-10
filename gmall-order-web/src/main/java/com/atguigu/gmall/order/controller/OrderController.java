package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.UserService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

    @RequestMapping("/submitOrder")
    @LoginRequired
    public String submitOrder(HttpServletRequest request, ModelMap modelMap, String addressId, String tradeCode) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        boolean tb = orderService.checkTradeCode(memberId, tradeCode);
        String orderSn = "gmall0722";
        // 购物车列表
        List<OmsCartItem> cartListFromCache = cartService.getCartListFromCache(memberId);
        if(tb) {
            // 生成订单
            OmsOrder omsOrder = new OmsOrder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = sdf.format(new Date());
            orderSn = orderSn + format + System.currentTimeMillis();
            omsOrder.setOrderSn(orderSn);
            omsOrder.setTotalAmount(getTotalAmout(cartListFromCache));
            omsOrder.setPayAmount(getTotalAmout(cartListFromCache));
            omsOrder.setMemberUsername(nickname);
            omsOrder.setMemberId(memberId);
            omsOrder.setCreateTime(new Date());
            omsOrder.setStatus("0");
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressById(addressId);
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverPostCode("1111");
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());

            // 封装订单信息
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            for (OmsCartItem omsCartItem : cartListFromCache) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                // 验证商品库存，从库存系统中查询
                omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                // 验证商品价格
                boolean b = skuService.checkPrice(omsCartItem);
                if(b) {
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                }else{
                    return "tradeFail";
                }
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                omsOrderItem.setProductId(omsCartItem.getProductId());
                omsOrderItem.setOrderSn(orderSn);
                omsOrderItems.add(omsOrderItem);
            }
            omsOrder.setOmsOrderItems(omsOrderItems);

            // 保存订单到数据库
            orderService.addOrder(omsOrder);

            // 删除购物车
            //cartService.removeCart(cartListFromCache);
        }else {
            return "tradeFail";
        }
        return "redirect:http://payment.gmall.com:8088/index?orderSn=" + orderSn + "&totalAmount=" + getTotalAmout(cartListFromCache);
    }


    @RequestMapping("/toTrade")
    @LoginRequired
    public String toTrade(HttpServletRequest request, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");

        // 购物车列表
        List<OmsCartItem> cartListFromCache = cartService.getCartListFromCache(memberId);

        // 收获地址列表
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getReceiveAddressByMemberId(memberId);

        List<OmsOrderItem> omsOrderItems = new ArrayList<>();

        for (OmsCartItem omsCartItem : cartListFromCache) {
            OmsOrderItem omsOrderItem = new OmsOrderItem();
            String orderSn = "gmall0722";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = sdf.format(new Date());
            orderSn = orderSn + format + System.currentTimeMillis();
            omsOrderItem.setOrderSn(orderSn);
            omsOrderItem.setProductId(omsCartItem.getProductId());
            omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
            omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
            omsOrderItem.setProductName(omsCartItem.getProductName());
            omsOrderItem.setProductPic(omsCartItem.getProductPic());
            omsOrderItem.setProductPrice(omsCartItem.getPrice());
            omsOrderItem.setProductQuantity(omsCartItem.getQuantity());

            omsOrderItems.add(omsOrderItem);
        }
        modelMap.put("orderDetailList", omsOrderItems);
        modelMap.put("userAddressList", umsMemberReceiveAddresses);
        modelMap.put("totalAmount", getTotalAmout(cartListFromCache));

        // 生成tradeCode校验码，写入结算页
        String tradeCode = orderService.getTradeCode(memberId);
        modelMap.put("tradeCode", tradeCode);
        return "trade";
    }

    private BigDecimal getTotalAmout(List<OmsCartItem> omsCartItems) {

        BigDecimal bigDecimal = new BigDecimal("0");
        if (omsCartItems != null && omsCartItems.size() > 0) {
            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    bigDecimal = bigDecimal.add(omsCartItem.getQuantity().multiply(omsCartItem.getPrice()));
                }
            }
        }
        return bigDecimal;
    }
}
