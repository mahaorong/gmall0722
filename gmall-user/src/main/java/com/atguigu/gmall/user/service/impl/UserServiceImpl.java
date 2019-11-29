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

    @Override
    public UmsMember login(UmsMember umsMember) {
        UmsMember umsMember1 = new UmsMember();
        umsMember1.setUsername(umsMember.getUsername());
        umsMember1.setPassword(umsMember.getPassword());
        return userMemberMapper.selectOne(umsMember1);
    }

    @Override
    public UmsMember addUser(UmsMember umsMember) {
        return null;
    }
}
