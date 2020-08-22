package com.caffe.monitor.alarm.pojo.bo;

import com.alibaba.fastjson.JSON;

/**
 * @author BitterCaffe
 * @date 2020/8/22
 * @description: monitor BO
 */

public class LogMonitorBO {

    private String traceId;

    private String fullInfo;

    private String message;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getFullInfo() {
        return fullInfo;
    }

    public void setFullInfo(String fullInfo) {
        this.fullInfo = fullInfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
