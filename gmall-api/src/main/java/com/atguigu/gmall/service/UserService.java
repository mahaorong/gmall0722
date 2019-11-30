package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUser();

    UmsMember getUser(String memeberId);

    UmsMember login(UmsMember umsMember);

    UmsMember addUser(UmsMember umsMember);

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    UmsMemberReceiveAddress getReceiveAddressById(String addressId);

    UmsMember isUserExists(UmsMember umsMember);
}
