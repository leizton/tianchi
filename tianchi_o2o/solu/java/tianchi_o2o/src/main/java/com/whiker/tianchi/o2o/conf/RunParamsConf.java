package com.whiker.tianchi.o2o.conf;

/**
 * @author whiker@163.com create on 16-11-2.
 */
public class RunParamsConf {

    private static int jdbcRDDPartitionNum;

    static {
        LoadConf.load("runparams.properties", conf -> {
            jdbcRDDPartitionNum = Integer.parseInt(conf.getProperty("jdbcRDDPartitionNum"));
        });
    }

    public static int getJdbcRDDPartitionNum() {
        return jdbcRDDPartitionNum;
    }
}
