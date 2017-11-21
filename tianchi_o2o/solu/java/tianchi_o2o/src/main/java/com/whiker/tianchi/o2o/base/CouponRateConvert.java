package com.whiker.tianchi.o2o.base;

import com.whiker.tianchi.o2o.common.Constants;
import com.whiker.tianchi.o2o.common.DateUtil;
import com.whiker.tianchi.o2o.conf.InputConf;
import com.whiker.tianchi.o2o.dao.BatchInsert;
import com.whiker.tianchi.o2o.model.base.OfflineTrainRow;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 统一优惠率后，把原始数据存入offline_train表中
 *
 * @author whiker@163.com create on 16-10-23.
 */
public class CouponRateConvert {
    private static final Logger LOGGER = LoggerFactory.getLogger(CouponRateConvert.class);

    private static final String INSERT_SQL = "INSERT INTO offline_train VALUES(NULL,?,?,?,?,?,?,?)";

    public static void main(String[] args) {
        SparkSession sess = SparkSession.builder()
                .appName("CouponRateConvert")
                .getOrCreate();

        JavaSparkContext sc = new JavaSparkContext(sess.sparkContext());
        sc.textFile(InputConf.getOfflineTrainInputFile())
                .map(CouponRateConvert::convert)
                .filter(row -> row != null)
                .foreachPartition(iter -> BatchInsert.insert(iter, INSERT_SQL, CouponRateConvert::insert));

        sess.stop();
    }

    private static OfflineTrainRow convert(String input) {
        try {
            String[] data = input.split(Constants.fileLineSeparator);
            OfflineTrainRow row = new OfflineTrainRow();
            row.setUserId(Integer.parseInt(data[0]));
            row.setMerchantId(Integer.parseInt(data[1]));
            row.setCouponId(data[2].equals("null") ? -1 : Integer.parseInt(data[2]));
            row.setCouponRate(row.getCouponId() >= 0 ? convertCouponRate(data[3]) : 0);
            row.setDistance(data[4].equals("null") ? -1 : Integer.parseInt(data[4]));
            row.setCouponGetDate(data[5].equals("null") ? -1 : DateUtil.toDate(data[5]));
            row.setConsumeDate(data[6].equals("null") ? -1 : DateUtil.toDate(data[6]));
            return row;
        } catch (Exception e) {
            LOGGER.error("convert error, input:{}", input, e);
            return null;
        }
    }

    static double convertCouponRate(String s) {
        if (s.contains(":")) {
            String[] vs = s.split(":");
            int v1 = Integer.parseInt(vs[0]);
            double v2 = Double.parseDouble(vs[1]);
            return (1.0 - v2 / v1);
        } else {
            return Double.parseDouble(s);
        }
    }

    private static void insert(OfflineTrainRow row, PreparedStatement ps) throws SQLException {
        ps.setInt(1, row.getUserId());
        ps.setInt(2, row.getMerchantId());
        ps.setInt(3, row.getCouponId());
        ps.setDouble(4, row.getCouponRate());
        ps.setInt(5, row.getDistance());
        ps.setInt(6, row.getCouponGetDate());
        ps.setInt(7, row.getConsumeDate());
    }
}
