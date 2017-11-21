package com.whiker.tianchi.o2o.bayes.forecast;

import com.whiker.tianchi.o2o.bayes.actionmean.ActionValue;
import com.whiker.tianchi.o2o.bayes.probability.BayesProbability;
import com.whiker.tianchi.o2o.dao.ConnectManager;
import com.whiker.tianchi.o2o.dao.JdbcRDDRead;
import com.whiker.tianchi.o2o.model.bayes.BayesForecastRow;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author whiker@163.com create on 16-11-9.
 */
public class BayesForecastRun {
    private static Logger LOGGER = LoggerFactory.getLogger(BayesForecastRun.class);

    private static AtomicInteger countProbLarge = new AtomicInteger(0);
    private static AtomicInteger countProbSmall = new AtomicInteger(0);
    private static AtomicInteger countProbTotal = new AtomicInteger(0);

    private static final int BATCH_SIZE = 10000;

    public static void main(String[] args) {
        BayesProbability.init();

        SparkSession sess = SparkSession.builder()
                .appName("BayesForecastRun")
                .getOrCreate();

        List<BayesForecastRow> rows = JdbcRDDRead
                .readAll(sess, "bayes_forecast", "*", BayesForecastRun::read)
                .map(row -> {
                    double negativeProb = 0;
                    for (int i = 0; i < ActionValue.negativeValues.length; i++) {
                        negativeProb += BayesProbability.compute(
                                ActionValue.negativeValues[i], row.discountGrade,
                                row.distanceGrade, row.userGrade, row.merchantGrade);
                    }
                    double positiveProb = 0;
                    for (int i = 0; i < ActionValue.positiveValues.length; i++) {
                        positiveProb += BayesProbability.compute(
                                ActionValue.positiveValues[i], row.discountGrade,
                                row.distanceGrade, row.userGrade, row.merchantGrade);
                    }
                    row.probablity = Math.max(0, Math.min(1, 1 - negativeProb));

                    double prob = negativeProb + positiveProb;
                    if (prob > 1.05) {
                        countProbLarge.getAndIncrement();
                    } else if (prob < 0.95) {
                        countProbSmall.getAndIncrement();
                    }
                    countProbTotal.getAndIncrement();
                    return row;
                })
                .collect();

        Iterator<BayesForecastRow> iter = rows.iterator();
        final String updateSql = "update bayes_forecast set probability=? where id=?";
        try {
            Connection conn = ConnectManager.openNewConnection();
            conn.setAutoCommit(false);
            while (iter.hasNext()) {
                PreparedStatement ps = conn.prepareStatement(updateSql);
                for (int i = 0; i < BATCH_SIZE && iter.hasNext(); i++) {
                    BayesForecastRow row = iter.next();
                    ps.setDouble(1, row.probablity);
                    ps.setInt(2, row.id);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                LOGGER.info("update");
            }
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("count: {}, {}", countProbLarge, countProbSmall);
    }

    static BayesForecastRow read(ResultSet r) throws SQLException {
        BayesForecastRow row = new BayesForecastRow();
        row.id = r.getInt(1);
        row.userId = r.getInt(2);
        row.merchantId = r.getInt(3);
        row.couponId = r.getInt(4);
        row.discount = r.getInt(5);
        row.distance = r.getInt(6);
        row.couponGetDate = r.getInt(7);
        row.userGrade = r.getInt(8);
        row.merchantGrade = r.getInt(9);
        row.discountGrade = r.getInt(10);
        row.distanceGrade = r.getInt(11);
        row.probablity = r.getDouble(12);
        return row;
    }
}
