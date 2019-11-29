package com.atguigu.gmall.passport.test;

import com.atguigu.gmall.utils.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) {

        String appKey = "906646988";

        String appSecret = "73363e89f0e4284feaca09c6e64c0950";

        // 授权回调地址
        String login = "http://passport.gmall.com/vlogin";

        // 取消授权回调页
        String logout = "http://passport.gmall.com/vlogout";

        // 地址1 用户登录需要跳转的地址
        String addr1 = "https://api.weibo.com/oauth2/authorize?client_id=906646988&response_type=code&redirect_uri=http://passport.gmall.com/vlogin";

        // 地址2 授权code回调，返回code
        String addr2 = "http://passport.gmall.com/vlogin?code=1c961aa711f676446e359918ba256b2a";

        // 地址3 使用返回的code，换取access_token
        String addr3 = "https://api.weibo.com/oauth2/access_token";//?client_id=906646988&client_secret=73363e89f0e4284feaca09c6e64c0950&grant_type=authorization_code&redirect_uri=http://passport.gmall.com/vlogin&code=1c961aa711f676446e359918ba256b2a";

        Map<String, String> map = new HashMap<>();
        map.put("client_id", "906646988");
        map.put("client_secret", "73363e89f0e4284feaca09c6e64c0950");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://passport.gmall.com/vlogin");
        map.put("code", "1c961aa711f676446e359918ba256b2a");

        HttpclientUtil.doPost(addr3, map);

        // {"access_token":"2.00nJ4twG0WSM3z4bb74bf666DOdAOC","remind_in":"157679998","expires_in":157679998,"uid":"6367021539","isRealName":"true"}
        String access_token = "2.00nJ4twG0WSM3z4bb74bf666DOdAOC";
        String uid = "6367021539";

        // 地址4 使用access_token调用开发api获取用户信息
        String addr4 = "https://api.weibo.com/2/users/show.json?access_token=2.00nJ4twG0WSM3z4bb74bf666DOdAOC&uid=6367021539";
    }
}
