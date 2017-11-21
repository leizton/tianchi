package com.whiker.tianchi.o2o.conf;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author whiker@163.com create on 16-11-10.
 */
public class OutputConf {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd-HHmm");

    private static String outputFilePrefix;

    static {
        LoadConf.load("output.properties", conf -> {
            outputFilePrefix = conf.getProperty("outputFilePrefix");
        });
    }

    public static String getOutputFileName() {
        return outputFilePrefix
                + "-" + DATE_FORMAT.print(DateTime.now().getMillis())
                + ".csv";
    }
}
