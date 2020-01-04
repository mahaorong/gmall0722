package com.atguigu.gmall.interceptors;

import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.utils.CookieUtil;
import com.atguigu.gmall.utils.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        // 拦截代码
        if(!(handler instanceof  HandlerMethod)) {
            return true;
        }
        // 判断被拦截的请求的访问的方法的注解(是否需要被拦截)
        HandlerMethod hm = (HandlerMethod)handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        // 没有这个注解直接通过
        if(methodAnnotation == null) {
            return true;
        }

        String token = "";

        // cookie中的token
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        // 地址中的token
        String newToken = request.getParameter("newToken");

        if(StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }

        if(StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        if(StringUtils.isBlank(token)) {
            if(methodAnnotation.isNeededSuccess() == false) {
                return true;
            }
            StringBuffer requestURL = request.getRequestURL();
            response.sendRedirect("http://passport.gmall.com:8090/index?returnUrl=" + requestURL);
            return false;
        }else {
            // 校验token 远程调用
//            String mapJson = HttpclientUtil.doGet("http://passport.gmall.com:8082/verify?token=" + token +"&currentIp=" + request.getRemoteAddr());
//            Map<String, String> map = new HashMap<>();
//            Map<String, String> map1 = JSON.parseObject(mapJson, map.getClass());
//
//            String memberId = map1.get("memberId");
//            String success = map1.get("success");

            // 校验token，jwt解析，解析结果中包含用户信息
            String remoteAddr = request.getRemoteAddr();
            Map<String, Object> gmall0722 = JwtUtil.decode(token, "gmall0722", remoteAddr);

            if(gmall0722 != null) {
                request.setAttribute("memberId", gmall0722.get("memberId"));
                request.setAttribute("nickname", gmall0722.get("nickname"));

                // 校验通过，刷新cookie过期时间
                CookieUtil.setCookie(request, response, "oldToken", token, 60*30, true);
            }else {
                if(methodAnnotation.isNeededSuccess() == false) {
                    return true;
                }
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8090/index?returnUrl=" + requestURL);

                return false;
            }
        }
       return true;
    }
}
