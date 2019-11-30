package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manager.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);

        String sku_id = pmsSkuInfo.getId();

        // 保存销售属性
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuInfo.getSkuSaleAttrValueList()) {
            pmsSkuSaleAttrValue.setSkuId(sku_id);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        // 保存平台属性
        for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuInfo.getSkuAttrValueList()) {
            pmsSkuAttrValue.setSkuId(sku_id);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        // 保存图片
        for (PmsSkuImage pmsSkuImage : pmsSkuInfo.getSkuImageList()) {
            pmsSkuImage.setSkuId(sku_id);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }
    }

    @Override
    public PmsSkuInfo item(String skuId) {

        PmsSkuInfo pmsSkuInfo = null;

        Jedis jedis = redisUtil.getJedis();

        // 缓存中查询
        String skuStr = jedis.get("sku:" + skuId + ":info");

        if (StringUtils.isNotBlank(skuStr)) {
            // 缓存中有数据
            pmsSkuInfo = JSON.parseObject(skuStr, PmsSkuInfo.class);
        } else {
            String lockId = UUID.randomUUID().toString();
            // 获得分布式锁
            String OK = jedis.set("sku:" + skuId + ":lock", lockId, "nx", "px", 10000);

            if (StringUtils.isNotBlank(OK) && "OK".equals(OK)) {

                // 查询db
                pmsSkuInfo = itemFromDb(skuId);

                // 将从db查询出来的放入缓存中
                jedis.set("sku:" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));

                // 删除锁之前先判断，当前的锁是不是当前线程的锁
//                String currentLockId = jedis.get("sku:" + skuId + ":lock");
//                if(StringUtils.isNotBlank(currentLockId)&&currentLockId.equals(lockId)){
//                    // 解锁
//                    jedis.del("sku:" + skuId + ":lock");
//                }

                // 使用lua脚本实现快速删锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                jedis.eval(script, Collections.singletonList("sku:" + skuId + ":lock"), Collections.singletonList(lockId));

            } else {
                // 自旋（过一定睡眠时间，重新访问方法）
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return item(skuId);
            }
        }

        jedis.close();

        return pmsSkuInfo;
    }

    public PmsSkuInfo itemFromDb(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfoFromDb = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        pmsSkuInfoFromDb.setSkuImageList(pmsSkuImages);

        return pmsSkuInfoFromDb;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());

            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(select);
        }
        return pmsSkuInfos;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        return pmsSkuInfoMapper.selectOne(pmsSkuInfo);
    }

    @Override
    public boolean checkPrice(OmsCartItem omsCartItem) {
        boolean b = false;
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(omsCartItem.getProductSkuId());
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        int i = pmsSkuInfo1.getPrice().compareTo(omsCartItem.getPrice());
        if(i == 0) {
            b = true;
        }
        return b;
    }

}
