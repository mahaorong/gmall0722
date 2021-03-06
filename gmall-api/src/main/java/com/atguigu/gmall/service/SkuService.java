package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;

import java.util.List;

public interface SkuService {

    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo item(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String spuId);

    List<PmsSkuInfo> getAllSku();

    PmsSkuInfo getSkuById(String skuId);

    boolean checkPrice(OmsCartItem omsCartItem);
}
