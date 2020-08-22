package com.caffe.monitor.alarm.filter;


import com.caffe.monitor.alarm.pojo.bo.LogMonitorBO;
import com.caffe.monitor.alarm.util.Log4jAlarm;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author BitterCaffe
 * @date 2020/8/22
 * @description: TODO
 */
public class Log4jMonitorFilter extends Filter {
    @Override
    public int decide(LoggingEvent loggingEvent) {
        // add in memory queue
        LogMonitorBO logMonitorBO = getMessage(loggingEvent);
        Log4jAlarm.addException(logMonitorBO);
        return Filter.NEUTRAL;
    }

    /**
     * get message
     *
     * @param loggingEvent
     */
    public LogMonitorBO getMessage(LoggingEvent loggingEvent) {
        // get info from event
        String traceId = String.valueOf(loggingEvent.getMDC("traceId"));
        String fullInfo = loggingEvent.getLocationInformation().fullInfo;
        String message = String.valueOf(loggingEvent.getMessage());

        // get log monitor bo bean
        LogMonitorBO logMonitorBO = new LogMonitorBO();
        logMonitorBO.setTraceId(traceId);
        logMonitorBO.setFullInfo(fullInfo);
        logMonitorBO.setMessage(message);
        return logMonitorBO;
    }


}
