package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.passport.utils.JwtUtil;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.utils.CookieUtil;
import com.atguigu.gmall.utils.HttpclientUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("/vlogin")
    public String vlogin(String code, HttpServletRequest request) {
        // 获得code

        // 交换access_code
        String addr3 = "https://api.weibo.com/oauth2/access_token";//?client_id=906646988&client_secret=73363e89f0e4284feaca09c6e64c0950&grant_type=authorization_code&redirect_uri=http://passport.gmall.com/vlogin&code=1c961aa711f676446e359918ba256b2a";

        Map<String, String> map = new HashMap<>();
        map.put("client_id", "906646988");
        map.put("client_secret", "73363e89f0e4284feaca09c6e64c0950");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://passport.gmall.com:8090/vlogin");
        map.put("code", code);

        String accessJSON = HttpclientUtil.doPost(addr3, map);

        // 获取access_token和uid
        Map<String, String> map1 = new HashMap<>();
        Map<String, String> map2 = JSON.parseObject(accessJSON, map1.getClass());
        String access_token = map2.get("access_token");
        String uid = map2.get("uid");

        // 交换用户信息
        String addr4 = "https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + uid;

        String userJSON = HttpclientUtil.doGet(addr4);
        Map<String, Object> map3 = JSON.parseObject(userJSON, map1.getClass());

        // 存储用户信息
        UmsMember umsMember = new UmsMember();
        umsMember.setSource_uid((String) map3.get("idstr"));
        umsMember.setSource_type("2");
        umsMember.setNickname((String) map3.get("screen_name"));
        umsMember.setCity((String) map3.get("location"));
        umsMember.setAccess_token(map2.get("access_token"));
        umsMember.setCreate_time(new Date());
        umsMember.setAccess_code(code);

        UmsMember umsMemberForRequest = new UmsMember();
        UmsMember umsMember2 = userService.isUserExists(umsMember);
        if(umsMember2 == null) {
            UmsMember umsMember1 = userService.addUser(umsMember);
            umsMemberForRequest = umsMember1;
        }else {
            umsMemberForRequest = umsMember;
        }

        // jwt， 生成token
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("memberId", umsMemberForRequest.getId());
        userMap.put("nickname", umsMemberForRequest.getNickname());
        String remoteAddr = request.getRemoteAddr();
        String token = JwtUtil.encode("gmall0722", userMap, remoteAddr);

        return "redirect:http://search.gmall.com:8083/index?newToken=" + token;
    }

    @RequestMapping("/verify")
    @ResponseBody
    public String verify(String token, HttpServletRequest request, String currentIp) {

        // 校验token，jwt解析，解析结果中包含用户信息
        Map<String, Object> gmall0722 = JwtUtil.decode(token, "gmall0722", currentIp);

        // 调用用户缓存服务，核对token
        HashMap<String, Object> map = new HashMap<>();
        if(gmall0722 != null) {
           map.put("memberId", gmall0722.get("memberId"));
           map.put("nickname", gmall0722.get("nickname"));
        }

        return JSON.toJSONString(map);
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(HttpServletRequest request, UmsMember umsMember) {

        // 判断用户名密码是否正确
        String cartListStr = CookieUtil.getCookieValue(request, "cartListCookie", true);
        UmsMember umsMemberFromDb = userService.login(umsMember, cartListStr);

        if(umsMemberFromDb != null) {
            // 调用用户服务，将token存入缓存服务器

            // jwt， 生成token
            Map<String, Object> map = new HashMap<>();
            map.put("memberId", umsMemberFromDb.getId());
            map.put("nickname", umsMemberFromDb.getNickname());
            String remoteAddr = request.getRemoteAddr();
            String token = JwtUtil.encode("gmall0722", map, remoteAddr);

            return token;
        }else {
         return "fail";
        }
    }

    @RequestMapping("/index")
    public String index(String returnUrl, ModelMap modelMap) {

        modelMap.put("ReturnUrl", returnUrl);
        return "index";
    }
}
