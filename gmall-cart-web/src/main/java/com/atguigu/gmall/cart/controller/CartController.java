package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@CrossOrigin
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    @RequestMapping("/checkCart")
    @LoginRequired(isNeededSuccess = false)
    public String checkCart(HttpServletRequest request, ModelMap modelMap, String isChecked, String skuId) {
        String memberId = "1";

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        if (StringUtils.isBlank(memberId)) {
            // 用户没登陆
            String cartListCookieStr = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookieStr)) {
                omsCartItems = JSON.parseArray(cartListCookieStr, OmsCartItem.class);
            }
        } else {
            // 用户登录了
            cartService.checkCart(memberId, isChecked, skuId);
            omsCartItems = cartService.getCartListFromCache(memberId);
        }
        modelMap.put("cartList", omsCartItems);
        // totalAmout
        BigDecimal totalAmout = getTotalAmout(omsCartItems);
        modelMap.put("totalAmout", totalAmout);
        return "cartListInner";
    }

    @RequestMapping("/cartList")
    @LoginRequired(isNeededSuccess = false)
    public String cartList(HttpServletRequest request, ModelMap modelMap) {
        String memberId = "1";
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        if (StringUtils.isBlank(memberId)) {
            // 用户没登陆
            String cartListCookieStr = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookieStr)) {
                omsCartItems = JSON.parseArray(cartListCookieStr, OmsCartItem.class);
            }
        } else {
            // 用户登录了
            omsCartItems = cartService.getCartListFromCache(memberId);
        }
        modelMap.put("cartList", omsCartItems);
        // totalAmout
        modelMap.put("totalAmout", getTotalAmout(omsCartItems));

        return "cartList";
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

    @RequestMapping("/addToCart")
    @LoginRequired(isNeededSuccess = false)
    public String addToCart(OmsCartItem omsCartItem, HttpServletRequest request, HttpServletResponse response) {
        // 商品数量
        BigDecimal quantity = omsCartItem.getQuantity();
        // 商品数据
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(omsCartItem.getProductSkuId());

        OmsCartItem omsCartItem1 = new OmsCartItem();
        omsCartItem1.setIsChecked("1");
        omsCartItem1.setProductSkuId(pmsSkuInfo.getId());
        omsCartItem1.setPrice(pmsSkuInfo.getPrice());
        omsCartItem1.setQuantity(quantity);
        omsCartItem1.setTotalPrice(quantity.multiply(omsCartItem1.getPrice()));
        omsCartItem1.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem1.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem1.setProductId(pmsSkuInfo.getProductId());
        omsCartItem1.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem1.setCreateDate(new Date());

        // 购物车集合变量
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        // 判断用户是否登录
        String memberId = "1";

        if (StringUtils.isBlank(memberId)) {
            /*// 创建购物车集合
            List<OmsCartItem> omsCartItems = null;
            // 用户未登录
            Cookie[] cookies = request.getCookies();
            if(cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    String name = cookie.getName();
                    if (name.equals("cartListCookie")) {
                        String value = cookie.getValue();

                        // 判断之前有没有添加过

                        omsCartItems = JSON.parseArray(value, OmsCartItem.class);
                    }
                }
            }
            // 覆盖cookie
            Cookie cookie = new Cookie("cartListCookie", JSON.toJSONString(omsCartItems));
            cookie.setMaxAge(10);
            response.addCookie(cookie);*/
            String cartListCookieStr = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isBlank(cartListCookieStr)) {
                // 购物车为空
                omsCartItems.add(omsCartItem1);
            } else {
                // 判断是否重复
                // 购物车里面数据
                omsCartItems = JSON.parseArray(cartListCookieStr, OmsCartItem.class);

                boolean b = if_new_cart(omsCartItems, omsCartItem);
                if (b) {
                    omsCartItems.add(omsCartItem1);
                } else {
                    // 更新
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                            cartItem.setTotalPrice(omsCartItem1.getPrice().multiply(cartItem.getQuantity()));
                        }
                    }
                }
            }
            // 覆盖cookie购物车里的数据
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 1000 * 60 * 30, true);
        } else {
            // 用户已登录
            // 查询数据库没有sku数据
            OmsCartItem omsCartItemFromDb = cartService.isCartExists(omsCartItem1);
            if (omsCartItemFromDb == null) {
                omsCartItem1.setMemberId(memberId);
                cartService.addCart(omsCartItem1);
            } else {
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem1.getQuantity()));
                omsCartItemFromDb.setTotalPrice(omsCartItemFromDb.getPrice().multiply(omsCartItemFromDb.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }
        }
        return "redirect:/success.html";
    }

    // 判断购物车是否有对应商品 true代表没有
    private boolean if_new_cart(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b = true;
        for (OmsCartItem cartItem : omsCartItems) {
            if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                b = false;
            }
        }
        return b;
    }
}
