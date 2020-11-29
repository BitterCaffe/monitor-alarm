package com.caffe.monitor.alarm.filter;

import com.caffe.monitor.alarm.pojo.dto.ErrorLogMonitorDTO;
import com.caffe.monitor.alarm.util.ErrorLogMonitorMulti;
import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author BitterCaffe
 * @date 2020/11/29
 * @description: TODO
 */
public class ErrorLogFilter extends Filter {
    @Override
    public int decide(LoggingEvent loggingEvent) {

        boolean flag = Level.ERROR.isGreaterOrEqual(loggingEvent.getLevel());
        if (!flag) {
            return Filter.NEUTRAL;
        }
        LocationInfo locationInfo = loggingEvent.getLocationInformation();
        long timestamp = loggingEvent.getTimeStamp();
        String msg = String.valueOf(loggingEvent.getMessage());
        String traceId = String.valueOf(loggingEvent.getMDC("traceId"));
        String fullInfo = locationInfo.fullInfo;
        String line = locationInfo.getClassName() + locationInfo.getMethodName();

        ErrorLogMonitorDTO errorLogMonitorDTO = ErrorLogMonitorDTO.instance();
        errorLogMonitorDTO.setTraceId(traceId);
        errorLogMonitorDTO.setTimestamp(String.valueOf(timestamp));
        errorLogMonitorDTO.setLine(line);
        errorLogMonitorDTO.setMsg(msg);
        errorLogMonitorDTO.setFullInfo(fullInfo);
        errorLogMonitorDTO.setLevel(1);
        ErrorLogMonitorMulti.syncAdd(errorLogMonitorDTO);
        return Filter.NEUTRAL;
    }
}
