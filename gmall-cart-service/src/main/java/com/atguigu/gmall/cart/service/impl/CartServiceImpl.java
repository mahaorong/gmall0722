package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.cart.mapper.OmsCartItemMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem isCartExists(OmsCartItem omsCartItem1) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(omsCartItem1.getMemberId());
        omsCartItem.setProductSkuId(omsCartItem1.getProductSkuId());
        return omsCartItemMapper.selectOne(omsCartItem);
    }

    @Override
    public void addCart(OmsCartItem omsCartItem1) {
        omsCartItemMapper.insertSelective(omsCartItem1);
        // 同步到缓存中
        Jedis jedis = null;

        jedis = redisUtil.getJedis();

        jedis.hset("user:" + omsCartItem1.getMemberId() + ":cart", omsCartItem1.getProductSkuId(), JSON.toJSONString(omsCartItem1));

        jedis.close();
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId", omsCartItemFromDb.getMemberId()).andEqualTo("productSkuId", omsCartItemFromDb.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb, example);

        // 同步到缓存中
        Jedis jedis = null;

        jedis = redisUtil.getJedis();

        jedis.hset("user:" + omsCartItemFromDb.getMemberId() + ":cart", omsCartItemFromDb.getProductSkuId(), JSON.toJSONString(omsCartItemFromDb));

        jedis.close();
    }

    @Override
    public List<OmsCartItem> getCartListFromCache(String memberId) {

        List<OmsCartItem> omsCartItems = new ArrayList<>();

        Jedis jedis = null;

        jedis = redisUtil.getJedis();

        List<String> hvals = jedis.hvals("user:" + memberId + ":cart");

        if (hvals != null && hvals.size() > 0) {
            for (String hval : hvals) {
                OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItems.add(omsCartItem);
            }
        }
        jedis.close();
        return omsCartItems;
    }

    @Override
    public void checkCart(String memberId, String isChecked, String skuId) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId", memberId).andEqualTo("productSkuId", skuId);

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setIsChecked(isChecked);
        omsCartItemMapper.updateByExampleSelective(omsCartItem, example);

        Jedis jedis = null;

        jedis = redisUtil.getJedis();

        String hget = jedis.hget("user:" + memberId + ":cart", skuId);
        if (StringUtils.isNotBlank(hget)) {
            OmsCartItem omsCartItem1 = JSON.parseObject(hget, OmsCartItem.class);
            omsCartItem1.setIsChecked(isChecked);
            jedis.hset("user:" + memberId + ":cart", skuId, JSON.toJSONString(omsCartItem1));
        }
        jedis.close();
    }
}
