package com.caffe.monitor.alarm.pojo.dto;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * @author BitterCaffe
 * @date 2020/11/29
 * @description: TODO
 */
public class ErrorLogMonitorDTO implements Serializable {
    private static final long serialVersionUID = 7766742472001793514L;

    private String traceId;

    /**
     * 异常提示信息
     */
    private String msg;

    /**
     * 生成的时间戳
     */
    private String timestamp;

    /**
     * 服务名称
     */
    private String serverName;

    /**
     * 环境： test  ,stage  , online
     */
    private String env;


    /**
     * 报警级别
     */
    private Integer level;

    /**
     * 业务线，按照这个进行报警限制
     * 这个可以是具体的业务线，也可以是方法名称
     * 可以按照具体需求来做
     */
    private String line;

    /**
     * 异常曝出信息
     */
    private String fullInfo;

    /**
     * 是否报警 默认报警
     */
    private String alarm;

    /**
     * build instance
     *
     * @return
     */
    public static ErrorLogMonitorDTO instance() {
        return new ErrorLogMonitorDTO();
    }


    private ErrorLogMonitorDTO() {
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getFullInfo() {
        return fullInfo;
    }

    public void setFullInfo(String fullInfo) {
        this.fullInfo = fullInfo;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }
}
