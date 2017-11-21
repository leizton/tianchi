package com.whiker.tianchi.o2o.dao;

import com.whiker.tianchi.o2o.conf.RunParamsConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.rdd.JdbcRDD;
import org.apache.spark.sql.SparkSession;

import java.sql.ResultSet;

/**
 * @author whiker@163.com create on 16-11-2.
 */
public class JdbcRDDRead {

    /**
     * 自增主键是"id"
     */
    public static <T> JavaRDD<T> readAll(SparkSession sess,
                                         String tableName, String columns,
                                         Function<ResultSet, T> mapRow) {
        return JdbcRDD.create(
                new JavaSparkContext(sess.sparkContext()),
                ConnectManager::getConnection,
                "select " + columns + " from " + tableName + " where ? <= id and id <= ?",
                1, Integer.MAX_VALUE,
                RunParamsConf.getJdbcRDDPartitionNum(),
                mapRow
        );
    }

    /**
     * 自定义查询sql
     */
    public static <T> JavaRDD<T> readAll(SparkSession sess,
                                         String sql,
                                         Function<ResultSet, T> mapRow) {
        return JdbcRDD.create(
                new JavaSparkContext(sess.sparkContext()),
                ConnectManager::getConnection,
                sql, 1, Integer.MAX_VALUE,
                RunParamsConf.getJdbcRDDPartitionNum(),
                mapRow
        );
    }
}
