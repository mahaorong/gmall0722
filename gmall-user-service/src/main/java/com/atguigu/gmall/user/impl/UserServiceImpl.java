package com.atguigu.gmall.user.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.user.mapper.UserMemberMapper;
import com.atguigu.gmall.utils.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.*;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMemberMapper userMemberMapper;

    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

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
    public UmsMember login(UmsMember umsMember, String cartListStr) {
        UmsMember umsMember1 = new UmsMember();
        umsMember1.setUsername(umsMember.getUsername());
        umsMember1.setPassword(umsMember.getPassword());
        UmsMember umsMember2 = userMemberMapper.selectOne(umsMember1);

        if(umsMember2 != null) {
            // 发送消息队列通知，其他业务服务，如购物车，站内信，短信，邮件，日志等
            if(StringUtils.isNotBlank(cartListStr)) {
                sendCartLogin(umsMember2, cartListStr);
            }
        }

        return umsMember2;
    }

    private void sendCartLogin(UmsMember umsMember, String cartListStr) {
        ConnectionFactory connect = activeMQUtil.getConnectionFactory();
        try {
            Connection connection = connect.createConnection();
            connection.start();
            Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("CART_LOGIN");

            MessageProducer producer = session.createProducer(queue);

//            ActiveMQTextMessage message = new ActiveMQTextMessage();
//            message.setText("支付完成，修改订单信息");

            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("memberId", umsMember.getId());
            mapMessage.setString("cartListStr", cartListStr);

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
            connection.close();
        }catch (JMSException e) {
            e.printStackTrace();
        }
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

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String addressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(addressId);
        return umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
    }

    @Override
    public UmsMember isUserExists(UmsMember umsMember) {
        UmsMember umsMember1 = new UmsMember();
        umsMember1.setId(umsMember.getId());
        return userMemberMapper.selectOne(umsMember1);
    }
}
