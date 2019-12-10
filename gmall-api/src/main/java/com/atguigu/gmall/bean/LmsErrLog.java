package com.atguigu.gmall.bean;

import javax.persistence.Column;
import javax.persistence.Id;

public class LmsErrLog {

    @Id
    private String id;
    @Column
    private String errLog;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getErrLog() {
        return errLog;
    }

    public void setErrLog(String errLog) {
        this.errLog = errLog;
    }
}
