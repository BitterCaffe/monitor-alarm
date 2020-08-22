package com.caffe.monitor.alarm.util;

import com.caffe.monitor.alarm.pojo.bo.MonitorAlarmByTimesBO;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author BitterCaffe
 * @date 2020/8/22
 * @description: 在一定时间内异常次数超过一定阀值之后进行报警
 */
public class MonitorAlarmByTimes {

    /**
     * max capacity of block queue
     */
    private static final Integer CAPACITY = 16;

    /**
     * one minute collect number of times greater than this value then alarm
     */
    private static Integer THRESHOLD = 3;

    /**
     * schedule delay time of minute
     */
    private static final Integer DELAY = 2;
    /**
     * add thread shutdown hook
     */
    private static volatile boolean RUN = true;


    /**
     * get concurrent key by KEY
     */
    private static String KEY = "%s_%d";


    private static volatile ScheduledThreadPoolExecutor executorService;

    private static final ArrayBlockingQueue<MonitorAlarmByTimesBO> QUEUE = new ArrayBlockingQueue<>(CAPACITY);

    /**
     * one application  one minute collect number of times
     */
    private static final ConcurrentMap<String, AtomicInteger> CONCURRENT = new ConcurrentHashMap<>(CAPACITY);


    static {
        init();
    }

    /**
     * 任务初始化
     */
    private static void init() {
        // customer
        new Thread(() -> execute()).start();

        //clear
        executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleWithFixedDelay(() -> clearCache(), 0, DELAY, TimeUnit.MINUTES);

        //add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> hook()));
    }

    /**
     * exception information collect
     *
     * @param monitorAlarmByTimesBO
     */
    public static void addException(MonitorAlarmByTimesBO monitorAlarmByTimesBO) {
        QUEUE.add(monitorAlarmByTimesBO);
    }

    /**
     * judgment one minute  collect number of times  and  alarm
     *
     * @param monitorAlarmByTimesBO
     */
    private static void customer(MonitorAlarmByTimesBO monitorAlarmByTimesBO) {
        String appName = monitorAlarmByTimesBO.getAppName();
        long currentMinute = getMinute();
        String key = String.format(MonitorAlarmByTimes.KEY, appName, currentMinute);
        AtomicInteger times = CONCURRENT.get(key);
        if (null == times) {
            times = new AtomicInteger(1);
            AtomicInteger res = CONCURRENT.putIfAbsent(key, new AtomicInteger(1));
            if (null != res) {
                times = res;
            }
        }

        if (times.get() >= THRESHOLD) {
            String message = getMessage(monitorAlarmByTimesBO);
            System.err.println("异常报警信息" + message);
            times.set(1);
            return;
        }
        times.incrementAndGet();
    }

    /**
     * get exception from  queue and customer
     */
    private static void execute() {
        while (RUN && !Thread.currentThread().isInterrupted()) {
            try {
                MonitorAlarmByTimesBO monitorAlarmByTimesBO = QUEUE.take();
                customer(monitorAlarmByTimesBO);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * clear map cache
     */
    private static void clearCache() {
        Set<String> keySet = CONCURRENT.keySet();
        long threshold = getMinute() - DELAY;
        for (String s : keySet) {
            long time = Long.parseLong(s.split("_")[1]);
            if (threshold > time) {
                CONCURRENT.remove(s);
            }
        }
    }

    /**
     * get alarm message
     *
     * @param monitorAlarmByTimesBO
     * @return
     */
    private static String getMessage(MonitorAlarmByTimesBO monitorAlarmByTimesBO) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("应用报警信息\n");
        stringBuffer.append("【").append(monitorAlarmByTimesBO.getAppName()).append("】:");
        stringBuffer.append("一分钟异常超").append(MonitorAlarmByTimes.THRESHOLD).append("次").append("\n");
        stringBuffer.append("【").append("异常信息】:").append(monitorAlarmByTimesBO.getError().getMessage());
        return stringBuffer.toString();
    }

    /**
     * get current minute num
     *
     * @return
     */
    private static long getMinute() {
        return System.currentTimeMillis() / 60000;
    }

    /**
     * add shutdown hook
     */
    private static void hook() {
        RUN = false;
        if (null != executorService) {
            executorService.shutdown();
        }
    }
}
