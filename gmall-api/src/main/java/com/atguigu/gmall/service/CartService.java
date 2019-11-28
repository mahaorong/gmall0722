package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {

    OmsCartItem isCartExists(OmsCartItem omsCartItem1);

    void addCart(OmsCartItem omsCartItem1);

    void updateCart(OmsCartItem omsCartItemFromDb);

    List<OmsCartItem> getCartListFromCache(String memberId);

    void checkCart(String memberId, String isChecked, String skuId);
}
