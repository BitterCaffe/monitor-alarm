import com.caffe.monitor.alarm.pojo.bo.MonitorAlarmByTimesBO;
import com.caffe.monitor.alarm.util.MonitorAlarmByTimes;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * @author BitterCaffe
 * @date 2020/8/22
 * @description: TODO
 */
public class TestMain {

    private static final Logger log = Logger.getLogger(TestMain.class);

    public static void main(String[] args) throws InterruptedException {
        TestMain testMain = new TestMain();
//        testMain.logFilterTest();
        testMain.alarmByTimes();
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
}
