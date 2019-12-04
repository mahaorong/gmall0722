package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.cart.mapper.OmsCartItemMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.utils.CookieUtil;
import com.atguigu.gmall.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Iterator;
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

    @Override
    public void removeCart(List<OmsCartItem> cartListFromCache) {

        Jedis jedis = null;

        jedis = redisUtil.getJedis();

        for (OmsCartItem omsCartItem : cartListFromCache) {
            if(omsCartItem.getIsChecked().equals("1")) {
                OmsCartItem omsCartItem1 = new OmsCartItem();
                omsCartItem1.setId(omsCartItem.getId());
                omsCartItemMapper.delete(omsCartItem1);
                jedis.hdel("user:" + omsCartItem.getMemberId() + ":cart", omsCartItem.getProductSkuId());
            }
        }
        jedis.close();
    }

    @Override
    public void mergCart(String memberId, String cartListStr) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        List<OmsCartItem> omsCartItemsList = JSON.parseArray(cartListStr, OmsCartItem.class);

        Jedis jedis = null;

        jedis = redisUtil.getJedis();

        List<OmsCartItem> omsCartItems1 = omsCartItemMapper.selectAll();
        if(omsCartItems1 != null) {
            for (OmsCartItem omsCartItem : omsCartItems1) {
                if(omsCartItemsList != null) {
                    Iterator<OmsCartItem> iterator = omsCartItemsList.iterator();
                    while (iterator.hasNext()) {
                        OmsCartItem omsCartItem1 = iterator.next();
                        if (omsCartItem.getMemberId().equals(memberId) && omsCartItem.getProductSkuId().equals(omsCartItem1.getProductSkuId())) {
                            omsCartItem.setQuantity(omsCartItem.getQuantity().add(omsCartItem1.getQuantity()));
                            Example example = new Example(OmsCartItem.class);
                            example.createCriteria().andEqualTo("memberId", omsCartItem.getMemberId()).andEqualTo("productSkuId", omsCartItem.getProductSkuId());
                            omsCartItemMapper.updateByExampleSelective(omsCartItem, example);
                            iterator.remove();
                        }
                    }
                }
            }
        }
        if(omsCartItemsList != null) {
            for (OmsCartItem omsCartItem : omsCartItemsList) {
                omsCartItem.setMemberId(memberId);
                omsCartItemMapper.insertSelective(omsCartItem);
                omsCartItems1.add(omsCartItem);
            }
        }
        for (OmsCartItem omsCartItem : omsCartItems1) {
            jedis.hset("user:" + memberId + ":cart", omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem));
        }
        jedis.close();
    }
}
