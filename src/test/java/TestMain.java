import com.caffe.monitor.alarm.pojo.bo.MonitorAlarmByTimesBO;
import com.caffe.monitor.alarm.pojo.dto.ErrorLogMonitorDTO;
import com.caffe.monitor.alarm.util.ErrorLogMonitorMulti;
import com.caffe.monitor.alarm.util.MonitorAlarmByTimes;
import org.apache.log4j.Logger;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * @author BitterCaffe
 * @date 2020/8/22
 * @description: TODO
 */
public class TestMain {

    private static final Logger log = Logger.getLogger(TestMain.class);

    public static void main(String[] args) throws InterruptedException {
//        TestMain testMain = new TestMain();
//        testMain.errorMonitorTest();
        String res = ErrorLogMonitorMulti.class.getClass().getResource("/").getPath();
        System.out.println("res:" + res);


    }

    /**
     * log filter
     */
    public void logFilterTest() {
        log.info("begin test log filter and alarm");
        log.error("get exception message and alarm");
    }

    /**
     * @throws InterruptedException
     */
    public void alarmByTimes() throws InterruptedException {
        String appName = "redisLock";
        Throwable throwable = new Throwable("test alarm by times");
        MonitorAlarmByTimesBO monitorAlarmByTimesBO =
                MonitorAlarmByTimesBO.builder().appName(appName).error
                        (throwable).build();

        for (int i = 0; i < 4; i++) {
            MonitorAlarmByTimes.addException(monitorAlarmByTimesBO);
            TimeUnit.MILLISECONDS.sleep(1);
        }
    }

    /**
     * multiTest
     *
     * @throws InterruptedException
     */
    public void multiTest() throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        // 业务1
        new Thread(() -> {
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 10; i++) {
                ErrorLogMonitorDTO bean = getMultiBean1(i);
                ErrorLogMonitorMulti.syncAdd(bean);
            }
        }).start();

        //业务2
        new Thread(() -> {
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 10; i++) {
                ErrorLogMonitorDTO bean = getMultiBean2(i);
                ErrorLogMonitorMulti.syncAdd(bean);
            }
        }).start();

        //业务三
        new Thread(() -> {
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 10; i++) {
                ErrorLogMonitorDTO bean = getMultiBean3(i);
                ErrorLogMonitorMulti.syncAdd(bean);
            }
        }).start();

        countDownLatch.await();
        System.out.println("over ……");
    }

    /**
     * 多业务线
     */
    public void errorMonitorTest() {
        ErrorLogMonitorDTO errorLogMonitorDTO = ErrorLogMonitorDTO.instance();
        errorLogMonitorDTO.setAlarm("alarm:true");
        errorLogMonitorDTO.setEnv("test");
        errorLogMonitorDTO.setFullInfo("fullInfo");
        errorLogMonitorDTO.setLevel(1);
        errorLogMonitorDTO.setLine("line");
        errorLogMonitorDTO.setMsg("error message");
        errorLogMonitorDTO.setServerName("server name");
        errorLogMonitorDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        errorLogMonitorDTO.setTraceId("traceId");
        ErrorLogMonitorMulti.syncAdd(errorLogMonitorDTO);
    }

    ErrorLogMonitorDTO getMultiBean1(int value) {
        ErrorLogMonitorDTO errorLogMonitorDTO = ErrorLogMonitorDTO.instance();
        errorLogMonitorDTO.setTraceId("traceId" + value);
        errorLogMonitorDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        errorLogMonitorDTO.setServerName("monitor-test");
        errorLogMonitorDTO.setMsg("error log monitor info " + value);
        errorLogMonitorDTO.setLine("业务线111");
        errorLogMonitorDTO.setLevel(1);
        errorLogMonitorDTO.setEnv("test");
        return errorLogMonitorDTO;
    }

    ErrorLogMonitorDTO getMultiBean2(int value) {
        ErrorLogMonitorDTO errorLogMonitorDTO = ErrorLogMonitorDTO.instance();
        errorLogMonitorDTO.setTraceId("traceId" + value);
        errorLogMonitorDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        errorLogMonitorDTO.setServerName("monitor-test");
        errorLogMonitorDTO.setMsg("error log  info " + value);
        errorLogMonitorDTO.setLine("业务线222");
        errorLogMonitorDTO.setLevel(1);
        errorLogMonitorDTO.setEnv("test");
        return errorLogMonitorDTO;
    }

    ErrorLogMonitorDTO getMultiBean3(int value) {
        ErrorLogMonitorDTO errorLogMonitorDTO = ErrorLogMonitorDTO.instance();
        errorLogMonitorDTO.setTraceId("traceId" + value);
        errorLogMonitorDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        errorLogMonitorDTO.setServerName("monitor-test");
        errorLogMonitorDTO.setMsg("error log monitor info " + value);
        errorLogMonitorDTO.setLine("业务线333");
        errorLogMonitorDTO.setLevel(1);
        errorLogMonitorDTO.setEnv("test");

        return errorLogMonitorDTO;
    }
}
