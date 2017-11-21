package com.whiker.tianchi.o2o.common;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author whiker@163.com create on 16-10-24.
 */
public class DateUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");

    private static final long START_TIMESTAMP = DATE_FORMAT.parseMillis("20151231");

    private static final long MILLISENCONDS_OF_ONE_DAY = 24 * 3600 * 1000;

    public static int toDate(String s) {
        long diff = DATE_FORMAT.parseMillis(s) - START_TIMESTAMP;
        return diff < 0 ? -1 : (int) (diff / MILLISENCONDS_OF_ONE_DAY);
    }

    public static String toString(int date) {
        long ts = date * MILLISENCONDS_OF_ONE_DAY + START_TIMESTAMP;
        return DATE_FORMAT.print(ts);
    }
}
