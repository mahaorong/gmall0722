package com.atguigu.gmall.user.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    @Reference
    private UserService userService;

    @ResponseBody
    @GetMapping("/getUser")
    public UmsMember getUser(String memeberId) {
        UmsMember umsMember = userService.getUser(memeberId);
        return umsMember;
    }

    @ResponseBody
    @GetMapping("/getAllUser")
    public List<UmsMember> getAllUser() {
        List<UmsMember> allUser = userService.getAllUser();
        return allUser;
    }
}
