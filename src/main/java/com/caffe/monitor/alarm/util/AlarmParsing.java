package com.caffe.monitor.alarm.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author BitterCaffe
 * @date 2020/11/29
 * @description: TODO
 */
public class AlarmParsing {
    private static final String regex = "(alarm(:|：)\\s{0,8}\\w+|\"alarm\"(:|：)\\s{0,8}\\w+|\"alarm\"(:|：)\\s{0,8}\"\\w+)";
    private static Pattern pattern = Pattern.compile(regex);

    /**
     * @param msg
     * @return
     */
    public static boolean alarm(String msg) {
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            String alarmMsg = matcher.group();
            int index = alarmMsg.indexOf("false");
            if (index > 0) {
                return false;
            }
        }
        return true;
    }
}
