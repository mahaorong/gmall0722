package com.atguigu.gmall.repair.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.LmsErrLog;
import com.atguigu.gmall.repair.mapper.LmsErrLogMapper;
import com.atguigu.gmall.service.ErrLogService;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ErrLogServiceImpl implements ErrLogService {

    @Autowired
    LmsErrLogMapper lmsErrLogMapper;

    @Override
    public void addErrLog(String s) {
        LmsErrLog lmsErrLog = new LmsErrLog();
        lmsErrLog.setErrLog(s);
        lmsErrLogMapper.insertSelective(lmsErrLog);
    }
}
