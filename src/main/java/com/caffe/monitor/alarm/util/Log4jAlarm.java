package com.caffe.monitor.alarm.util;

import com.caffe.monitor.alarm.pojo.bo.LogMonitorBO;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author BitterCaffe
 * @date 2020/8/22
 * @description: 内存队列+异步消费
 */
public class Log4jAlarm {
    /**
     * array block query max capacity
     */
    private static final Integer MAX_CAPACITY = 10;

    /**
     * alarm max times of on minute
     */
    private static final Integer MAX_ALARM = 10;

    /**
     * async exclude alarm
     */
    private static volatile boolean RUN = true;

    /**
     * Exception  message cache
     */
    private static final ArrayBlockingQueue<LogMonitorBO> QUEUE = new ArrayBlockingQueue<LogMonitorBO>(MAX_CAPACITY);

    /**
     * execute alarm task
     */
    static {
        new Thread(() -> {
            try {
                doAlarm();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
        }));
    }

    /**
     * add exception info
     *
     * @param logMonitorBO
     */
    public static void addException(LogMonitorBO logMonitorBO) {
        QUEUE.offer(logMonitorBO);
    }

    /**
     * consume  message and alarm
     */
    private static void doAlarm() throws InterruptedException {
        int minute = getMinute();
        int count = 0;
        while (RUN && !Thread.currentThread().isInterrupted()) {
            LogMonitorBO logMonitorBO = QUEUE.take();
            int currentMinute = getMinute();
            if (currentMinute > minute) {
                count = 0;
                minute = currentMinute;
            }
            if (count > MAX_ALARM) {
                TimeUnit.MILLISECONDS.sleep(3);
                continue;
            }
            String message = getAlarm(logMonitorBO);
            //调用报警接口进行报警
            System.err.println("报警信息:\n" + message);
            count++;
        }
    }

    /**
     * get message
     *
     * @param logMonitorBO
     * @return
     */
    private static String getAlarm(LogMonitorBO logMonitorBO) {
        String traceId = logMonitorBO.getTraceId();
        String fullInfo = logMonitorBO.getFullInfo();
        String message = logMonitorBO.getMessage();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("【title】:").append("[异常日志报警]").append("\n");
        stringBuffer.append("【traceId】:").append("[").append(traceId).append("]").append("\n");
        stringBuffer.append("【fullInfo】:").append("[").append(fullInfo).append("]").append("\n");
        stringBuffer.append("【message】:").append("[").append(message).append("]");
        String res = stringBuffer.toString();
        return res;
    }

    /**
     * get minute
     *
     * @return
     */
    private static int getMinute() {
        return (int) System.currentTimeMillis() / 600000;
    }

    /**
     * Runtime.getRuntime() add Shutdown hook
     */
    private static void stop() {
        RUN = false;
    }

}
