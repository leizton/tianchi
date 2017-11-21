package com.whiker.tianchi.o2o.bayes.forecast;

import com.whiker.tianchi.o2o.common.DateUtil;
import com.whiker.tianchi.o2o.conf.OutputConf;
import com.whiker.tianchi.o2o.dao.JdbcRDDRead;
import com.whiker.tianchi.o2o.model.bayes.BayesForecastRow;
import org.apache.spark.sql.SparkSession;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author whiker@163.com create on 16-11-10.
 */
public class BayesForecastExport {

    public static void main(String[] args) {
        SparkSession sess = SparkSession.builder()
                .appName("BayesForecastExport")
                .getOrCreate();

        List<BayesForecastRow> rows = JdbcRDDRead
                .readAll(sess, "bayes_forecast", "*", BayesForecastRun::read)
                .collect();
        output(rows);
    }

    private static void output(List<BayesForecastRow> rows) {
        String filename = OutputConf.getOutputFileName();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)))) {
            for (BayesForecastRow row : rows) {
                writer.write(row.userId + "," + row.couponId + ","
                        + DateUtil.toString(row.couponGetDate) + ","
                        + String.format("%.4f", row.probablity)
                );
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("save error", e);
        }
    }
}
