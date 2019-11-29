package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;

@Controller
@CrossOrigin
public class ItemController {

    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap) {
        PmsSkuInfo pmsSkuInfo = skuService.item(skuId);

        String spuId = pmsSkuInfo.getSpuId();

        // 匹配销售属性hash表
        List<PmsSkuInfo> pmsSkuInfosForHash = skuService.getSkuSaleAttrValueListBySpu(spuId);

        HashMap<String, String> hashMap = new HashMap<>();

        for (PmsSkuInfo skuInfosForHash : pmsSkuInfosForHash) {
            String v = skuInfosForHash.getId();

            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfosForHash.getSkuSaleAttrValueList();

            String k = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                String saleAttrValueId = pmsSkuSaleAttrValue.getSaleAttrValueId();
                k = k + "|" + saleAttrValueId;
            }

            hashMap.put(k, v);
        }

        String hashMapStr = JSON.toJSONString(hashMap);

        modelMap.put("hashMapStr", hashMapStr);

        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.getSpuSaleAttrListCheckBySku(spuId, pmsSkuInfo.getId());

        modelMap.put("skuInfo", pmsSkuInfo);
        modelMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);
        return "item";
    }

    /*@RequestMapping("/")
    public String items(@PathVariable String skuId, ModelMap modelMap) {
        // 匹配销售属性hash表
        List<PmsSkuInfo> pmsSkuInfosForHash = skuService.getSkuSaleAttrValueListBySpu(spuId);

        HashMap<String, String> hashMap = new HashMap<>();

        for (PmsSkuInfo skuInfosForHash : pmsSkuInfosForHash) {
            String v = skuInfosForHash.getId();

            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfosForHash.getSkuSaleAttrValueList();

            String k = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                String saleAttrValueId = pmsSkuSaleAttrValue.getSaleAttrValueId();
                k = k + "|" + saleAttrValueId;
            }

            hashMap.put(k, v);
        }

        String hashMapStr = JSON.toJSONString(hashMap);

        modelMap.put("hashMapStr", hashMapStr);
    }*/
}
