package com.whiker.tianchi.o2o.bayes.tablebuild;

import com.whiker.tianchi.o2o.bayes.grade.DiscountGradeGet;
import com.whiker.tianchi.o2o.bayes.grade.DistanceGradeGet;
import com.whiker.tianchi.o2o.bayes.grade.MerchantActionGradeGet;
import com.whiker.tianchi.o2o.bayes.grade.UserActionGradeGet;
import com.whiker.tianchi.o2o.dao.BatchInsert;
import com.whiker.tianchi.o2o.dao.JdbcRDDRead;
import com.whiker.tianchi.o2o.model.bayes.BayesForecastRow;
import org.apache.spark.sql.SparkSession;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author whiker@163.com create on 16-11-8.
 */
public class BayesForecastTableBuild {

    public static void main(String[] args) {
        UserActionGradeGet.init();
        MerchantActionGradeGet.init();

        SparkSession sess = SparkSession.builder()
                .appName("BayesTrainTableBuild")
                .getOrCreate();

        final String inputColumns = "user_id, merchant_id, coupon_id, coupon_rate, distance, coupon_get_date";
        final String outputSql = "insert into bayes_forecast values(NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0.0)";

        JdbcRDDRead.readAll(sess, "forecast", inputColumns, BayesForecastTableBuild::read)
                .filter(row -> row != null)
                .foreachPartition(iter -> BatchInsert.insert(iter, outputSql, BayesForecastTableBuild::insertBayesForecastRow));
    }

    private static BayesForecastRow read(ResultSet r) throws SQLException {
        BayesForecastRow row = new BayesForecastRow();
        row.userId = r.getInt(1);
        row.merchantId = r.getInt(2);
        row.couponId = r.getInt(3);
        if (row.couponId < 0) {
            return null;
        }

        int couponRate = (int) (r.getDouble(4) * 100);
        couponRate = Math.max(0, Math.min(100, couponRate));
        row.discount = 20 - couponRate / 5;
        row.distance = r.getInt(5);
        row.couponGetDate = r.getInt(6);

        row.userGrade = UserActionGradeGet.idToGrade(row.userId);
        row.merchantGrade = MerchantActionGradeGet.idToGrade(row.merchantId);
        row.discountGrade = DiscountGradeGet.toGrade(row.discount);
        row.distanceGrade = DistanceGradeGet.toGrade(row.distance);
        return row;
    }

    private static void insertBayesForecastRow(BayesForecastRow row, PreparedStatement ps) throws SQLException {
        ps.setInt(1, row.userId);
        ps.setInt(2, row.merchantId);
        ps.setInt(3, row.couponId);

        ps.setInt(4, row.discount);
        ps.setInt(5, row.distance);
        ps.setInt(6, row.couponGetDate);

        ps.setInt(7, row.userGrade);
        ps.setInt(8, row.merchantGrade);
        ps.setInt(9, row.discountGrade);
        ps.setInt(10, row.distanceGrade);
    }
}
