package com.caffe.monitor.alarm.util;

import com.caffe.monitor.alarm.pojo.dto.ErrorLogMonitorDTO;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author BitterCaffe
 * @author BitterCaffe
 * @date 2020/11/29
 * @description: 现有功能实现，存在问题，后期优化点描述
 * <p>
 * /**
 * @date 2020/11/25
 * @description: 现有功能实现，存在问题，后期优化点描述
 * <p>
 * <p>
 * <p>
 * 现有功能：
 * 1、按照业务线收集异常信息，业务线可以是整个项目维度，类维度，方法维度
 * 2、报警阀值也是按照业务线维度
 * 3、可以配置error级别的日志是否要报警，可以配置默认是否报警，也可以在error日志中添加关键词进行报警
 * 4、队列、阀值的值优先使用环境变量中的值，之后使用配置文件中的值，最后使用硬编码默认值
 * 5、队列、阀值可以动态扩容
 * 6、队列收集异常日志信息线程安全
 * <p>
 * <p>
 * <p>
 * 存在问题：
 * 1、扩容是统一扩容
 * 2、扩容之后没有将原有数据复制到新队列
 * 3、报警阀值是统一的有可能丢失关键报警信息
 * 4、队列或业务线存在僵尸情况，占用内存无法释放
 * <p>
 * <p>
 * <p>
 * 后期优化：
 * 1、分析各个业务线报警数据
 * 2、扩容按照各个业务线报警量进行扩容
 * 3、报警阀值按照业务线队列大小来计算或者按照丢失量量设置
 * 4、队列、阀值的扩容、僵尸数据收集也可以参考MySQL的innodb_buffer_pool、undo log_buffer_pool 的prue线程方式或redis的过期删除那种策略
 */
public class ErrorLogMonitorMulti {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ErrorLogMonitorMulti.class);

    private static String serverName;

    private static String IP;

    private static String env;

    /**
     * 异常消息存储队列
     */
    private static volatile int default_capacity = 1 << 4;

    /**
     * 周期内报警次数
     */
    private static volatile int default_frequency = 1 << 4;

    private static volatile boolean flag = true;

    /**
     * 业务线报警次数
     */
    private static volatile Map<String, AtomicInteger> lineMapCount = new HashMap<String, AtomicInteger>(32, 0.75F);

    /**
     * 异常消息存储
     */
    private static volatile ConcurrentMap<String, BlockingQueue<ErrorLogMonitorDTO>> lineMapQueue = new
            ConcurrentHashMap<>(32, 0.75F);

    private static volatile Set<String> lineSet = new CopyOnWriteArraySet<String>();

    private static Thread sendThread;

    private static Thread monitorThread;

    static {
        ErrorLogMonitorMulti.ResourceInfo.assign();
        sendThread = new Thread(() -> sendMessage());
        monitorThread = new Thread(new Monitor());
        sendThread.setName("supplier_ops_send_thread");
        monitorThread.setName("supplier_ops_monitor_thread");
        sendThread.start();
        monitorThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
    }


    /**
     * multi thread safe method
     *
     * @param errorLogMonitorDTO
     */
    public static void syncAdd(ErrorLogMonitorDTO errorLogMonitorDTO) {
        String line = errorLogMonitorDTO.getLine();
        lineSet.add(line);
        BlockingQueue<ErrorLogMonitorDTO> blockingQueue = lineMapQueue.get(line);
        if (null == blockingQueue) {
            BlockingQueue<ErrorLogMonitorDTO> newBlockQueue = lineMapQueue.putIfAbsent(line, blockingQueue = new
                    ArrayBlockingQueue<>(ErrorLogMonitorMulti.default_capacity));
            if (null != newBlockQueue) {
                blockingQueue = newBlockQueue;
            }
        }
        blockingQueue.offer(errorLogMonitorDTO);
    }

    private static void sendMessage() {

        long cycle = getMinute();
        while (flag && !Thread.currentThread().isInterrupted()) {
            try {
                Iterator iterator = lineMapQueue.entrySet().iterator();
                long count = 0;
                long beginTime = System.currentTimeMillis();
                while (iterator.hasNext()) {
                    Map.Entry<String, BlockingDeque<ErrorLogMonitorDTO>> entry = (Map.Entry<String, BlockingDeque<ErrorLogMonitorDTO>>)
                            iterator.next();
                    String line = entry.getKey();
                    BlockingQueue<ErrorLogMonitorDTO> blockingQueue = entry.getValue();
                    int size = blockingQueue.size();
                    for (int i = 0; i < size; i++) {
                        try {
                            ErrorLogMonitorDTO errorLogMonitorDTO = blockingQueue.poll();
                            if (null == errorLogMonitorDTO) {
                                continue;
                            }
                            count++;
                            long currentMinute = getMinute();
                            AtomicInteger lineCount = lineMapCount.get(line);
                            if (null == lineCount) {
                                lineCount = new AtomicInteger(0);
                                lineMapCount.put(line, lineCount);
                            }
                            if (cycle == currentMinute) {
                                if (lineCount.get() > default_frequency) {
                                    logger.info("current lineCount more than count line:{}", line);
                                    continue;
                                }
                            }
                            if (currentMinute > cycle) {
                                lineCount.set(0);
                                cycle = currentMinute;
                            }
                            doSend(errorLogMonitorDTO);
                            lineCount.incrementAndGet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (count == 0) {
                    TimeUnit.SECONDS.sleep(10);
                } else {
                    long oneCount = count / lineSet.size();
                    long ontTime = (System.currentTimeMillis() - beginTime) / oneCount;
                    if (ontTime < 1000) {
                        TimeUnit.SECONDS.sleep(3);
                    } else {
                        TimeUnit.MILLISECONDS.sleep(ontTime / default_capacity);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static void doSend(ErrorLogMonitorDTO errorLogMonitorDTO) {
        String msg = errorLogMonitorDTO.getMsg();
        boolean flag = AlarmParsing.alarm(msg);
        if (flag) {
            String message = doGetMsg(errorLogMonitorDTO);
            //报警信息
            logger.info("报警信息message:{}", message);
        }
    }

    private static String doGetMsg(ErrorLogMonitorDTO errorLogMonitorDTO) {
        String level = new StringBuffer().append("P").append(errorLogMonitorDTO.getLevel()).toString();
        String time = errorLogMonitorDTO.getTimestamp();
        long longTime = Long.valueOf(time);
        StringBuffer buffer = new StringBuffer();
        buffer.append("异常错误报警信息：").append("\n");
        buffer.append("报警环境：").append(env).append("\n");
        buffer.append("报警级别：").append(level).append("\n");
        buffer.append("服务名称：").append(serverName).append("-").append(IP).append("\n");
        buffer.append("traceId：").append(errorLogMonitorDTO.getTraceId()).append("\n");
        buffer.append("发生时间：").append(longTime).append("\n");
        buffer.append("异常定位：").append(errorLogMonitorDTO.getFullInfo()).append("\n");
        buffer.append("异常信息：").append(errorLogMonitorDTO.getMsg()).append("\n");
        return buffer.toString();
    }

    private static void shutdown() {
        if (null != sendThread && !sendThread.isInterrupted()) {
            sendThread.interrupt();
        }
        if (null != monitorThread && !monitorThread.isInterrupted()) {
            monitorThread.interrupt();
        }
        flag = false;
    }

    private static long getMinute() {
        return System.currentTimeMillis() / 1000 / 60;
    }


    private static class Monitor implements Runnable {

        @Override
        public void run() {
            ReLoad.reload();
        }
    }

    private static class ReLoad {
        /**
         * reload and replace
         */
        private static void reload() {
            String url = ErrorLogMonitorMulti.ResourceInfo.getFilePath();
            long beginTime = System.currentTimeMillis();
            while (flag && !Thread.currentThread().isInterrupted()) {
                try {
                    long time = new File(url).lastModified();
                    if (time > beginTime) {
                        beginTime = time;
                        InputStream in = new BufferedInputStream(new FileInputStream(url));
                        Properties properties = new Properties();
                        properties.load(in);
                        Integer capacity = Integer.valueOf(properties.getProperty("default_capacity"));
                        Integer frequency = Integer.valueOf(properties.getProperty("default_frequency"));
                        logger.info("monitor thread reload properties info change time:{},capacity:{},frequency:{}",
                                time, capacity,
                                frequency);
                        ErrorLogMonitorMulti.default_frequency = frequency;
                        if (capacity > ErrorLogMonitorMulti.default_capacity) {
                            for (String s : lineSet) {
                                lineMapQueue.put(s, new ArrayBlockingQueue<ErrorLogMonitorDTO>(capacity));
                            }
                            logger.info("block queue already  use new info frequency:{},capacity:{}", frequency, capacity);
                        }
                    }
                    TimeUnit.SECONDS.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private static class ResourceInfo {
        private static String getFilePath() {
            String fileName_prop = "/prop/application.properties";
            String fileName = "/application.properties";
            File file = new File(ErrorLogMonitorMulti.class.getClass().getResource("/").getPath());
            String url = file.getPath() + fileName;
            File fileExist = new File(url);
            boolean exists = fileExist.exists();
            if (!exists) {
                url = file.getPath() + fileName_prop;
            }
            return url;
        }

        private static Properties getProperties() {
            String url = getFilePath();
            try {
                InputStream inputStream = new BufferedInputStream(new FileInputStream(url));
                Properties properties = new Properties();
                properties.load(inputStream);
                return properties;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private static void assign() {
            //environment,server name ,ip init
            init();
            // capacity  system,properties,default
            String systemCapacity = System.getProperty("default_capacity", null);
            String systemFrequency = System.getProperty("default_frequency", null);
            Properties properties = null;
            if (StringUtils.isEmpty(systemCapacity)) {
                properties = getProperties();
                if (null != properties) {
                    String propertiesCapacity = properties.getProperty("default_capacity");
                    if (StringUtils.isNotEmpty(propertiesCapacity)) {
                        default_capacity = Integer.valueOf(propertiesCapacity.trim());
                    }
                }

            } else {
                default_capacity = Integer.valueOf(systemCapacity.trim());
            }

            if (StringUtils.isEmpty(systemFrequency)) {
                if (null != properties) {
                    String propertiesFrequency = properties.getProperty("default_frequency");
                    if (StringUtils.isNotEmpty(propertiesFrequency)) {
                        default_frequency = Integer.valueOf(propertiesFrequency.trim());
                    }
                } else {
                    properties = getProperties();
                    if (null != properties) {
                        String propertiesFrequency = properties.getProperty("default_frequency");
                        if (!StringUtils.isEmpty(propertiesFrequency)) {
                            default_frequency = Integer.valueOf(propertiesFrequency.trim());
                        }
                    }
                }
            } else {
                default_frequency = Integer.valueOf(systemFrequency.trim());
            }
        }

        private static void init() {
            IP = IPUtil.getLocalAddress();
            Properties properties = getProperties();
            serverName = properties.getProperty("application.name");
            env = properties.getProperty("dubbo.environment");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        logger.error("alarm:true");
        ErrorLogMonitorDTO errorLogMonitorDTO = ErrorLogMonitorDTO.instance();
        errorLogMonitorDTO.setTraceId("traceId");
        errorLogMonitorDTO.setMsg("alarm:true");
        errorLogMonitorDTO.setLine("line");
        errorLogMonitorDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        ErrorLogMonitorMulti.syncAdd(errorLogMonitorDTO);
        countDownLatch.await();
    }

}
