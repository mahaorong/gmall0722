package com.atguigu.gmall.user.service.impl;


import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMemberMapper userMemberMapper;

    @Override
    public List<UmsMember> getAllUser() {
        return userMemberMapper.selectAll();
}

    @Override
    public UmsMember getUser(String memeberId) {
        UmsMember umsMember = new UmsMember();
        umsMember.setId(memeberId);
        return userMemberMapper.selectOne(umsMember);
    }
}