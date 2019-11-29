package com.atguigu.gmall.user.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.user.mapper.UserMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMemberMapper userMemberMapper;

    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

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
        userMemberMapper.insertSelective(umsMember);
        return umsMember;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);

        return umsMemberReceiveAddresses;
    }
}
