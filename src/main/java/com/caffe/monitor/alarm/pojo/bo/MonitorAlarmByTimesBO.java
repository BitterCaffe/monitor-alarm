package com.caffe.monitor.alarm.pojo.bo;

import lombok.Builder;

/**
 * @author BitterCaffe
 * @date 2020/8/22
 * @description: 每一个应用按照一段时间内报异常次数来报警
 */

@Builder
public class MonitorAlarmByTimesBO {


    /**
     * application name
     */
    private String appName;


    /**
     * exception message
     */
    private Throwable error;


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }


    @Override
    public String toString() {
        return new StringBuffer().append("appName=").append(appName).append(",error=").append(error.getMessage())
                .toString();
    }

}
