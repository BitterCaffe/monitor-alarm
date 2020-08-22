import org.apache.log4j.Logger;

/**
 * @author BitterCaffe
 * @date 2020/8/22
 * @description: TODO
 */
public class TestMain {

    private static final Logger log = Logger.getLogger(TestMain.class);

    public static void main(String[] args) {
        log.info("begin test log filter and alarm");
        log.error("get exception message and alarm");
    }
}
