package com.whiker.tianchi.o2o.conf;

/**
 * @author whiker@163.com create on 16-10-29.
 */
public class InputConf {

    private static String offlineTrainInputFile;
    private static String forecastInputFile;

    static {
        LoadConf.load("input.properties", conf -> {
            offlineTrainInputFile = conf.getProperty("offlineTrainInputFile");
            forecastInputFile = conf.getProperty("forecastInputFile");
        });
    }

    public static String getOfflineTrainInputFile() {
        return offlineTrainInputFile;
    }

    public static String getForecastInputFile() {
        return forecastInputFile;
    }
}
