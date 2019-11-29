package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@CrossOrigin
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.search(pmsSearchParam);

        Set<String> strings = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                strings.add(valueId);
            }
        }

        String join = StringUtils.join(strings, ",");
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrListByProduct(join);

        // 删除选中属性值的属性
        String[] valueId = pmsSearchParam.getValueId();
        if (valueId != null && valueId.length > 0) {
            // 生成面包屑集合
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            // s是选中的valuedId属性值
            for (String s : valueId) {
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                String urlParam = getUrlParam(pmsSearchParam, s);
                pmsSearchCrumb.setValueId(s);
                pmsSearchCrumb.setUrlParam(urlParam);
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        if (s.equals(pmsBaseAttrValue.getId())) {
                            // remove后取不到valueName
                            pmsSearchCrumb.setValueName(pmsBaseAttrInfo.getAttrName());
                            iterator.remove();
                        }
                    }
                }
                // 塞入面包屑集合
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
        }

        // 面包屑
//        if (valueId != null && valueId.length > 0) {
//            // 生成面包屑集合
//            ArrayList<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
//            for (String s : valueId) {
//                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
//                String urlParam = getUrlParam(pmsSearchParam, s);
//                pmsSearchCrumb.setValueId(s);
//                pmsSearchCrumb.setUrlParam(urlParam);
//                pmsSearchCrumb.setValueName(s + "属性值的名称");
//                pmsSearchCrumbs.add(pmsSearchCrumb);
//            }
//            modelMap.put("attrValueSelectList", pmsSearchCrumbs);
//        }

        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);
        // 如果只剩一个属性则不显示
        if(pmsBaseAttrInfos != null && pmsBaseAttrInfos.size() > 1) {
            modelMap.put("attrList", pmsBaseAttrInfos);
        }
        // 拼接url
        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);
        return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam, String... idDel) {

        String urlParam = "";

        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] valueId = pmsSearchParam.getValueId();

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if (valueId != null && valueId.length > 0) {
            for (String id : valueId) {
                // 当数组为空时不等于null，等于0
                if (idDel == null || idDel.length == 0) {
                    urlParam = urlParam + "&valueId=" + id;
                } else {
                    if (!id.equals(idDel[0])) {
                        urlParam = urlParam + "&valueId=" + id;
                    }
                }
            }
        }
        return urlParam;
    }
}
