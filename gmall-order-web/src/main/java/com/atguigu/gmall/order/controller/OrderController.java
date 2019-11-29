package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.UserService;
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

    @RequestMapping("/toTrade")
    @LoginRequired
    public String toTrade(HttpServletRequest request, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");

        // 购物车列表
        List<OmsCartItem> cartListFromCache = cartService.getCartListFromCache(memberId);

        // 收获地址列表
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getReceiveAddressByMemberId(memberId);

        ArrayList<OmsOrderItem> omsOrderItems = new ArrayList<>();

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

        return "trade";
    }

    private BigDecimal getTotalAmout(List<OmsCartItem> omsCartItems) {

        BigDecimal bigDecimal = new BigDecimal("0");
        if (omsCartItems != null && omsCartItems.size() > 0) {
            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    bigDecimal = bigDecimal.add(omsCartItem.getTotalPrice());
                }
            }
        }
        return bigDecimal;
    }
}
