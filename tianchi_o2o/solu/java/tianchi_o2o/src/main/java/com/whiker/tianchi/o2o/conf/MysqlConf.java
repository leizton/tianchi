package com.whiker.tianchi.o2o.conf;

/**
 * @author whiker@163.com create on 16-10-29.
 */
public class MysqlConf {

    private static String jdbcUrl;

    static {
        LoadConf.load("mysql.properties", conf -> {
            jdbcUrl = conf.getProperty("jdbcUrl");
        });
    }

    public static String getJdbcUrl() {
        return jdbcUrl;
    }
}
