package com.whiker.tianchi.o2o.bayes.tablebuild;

import com.whiker.tianchi.o2o.bayes.grade.DiscountGradeGet;
import com.whiker.tianchi.o2o.bayes.grade.DistanceGradeGet;
import com.whiker.tianchi.o2o.bayes.grade.MerchantActionGradeGet;
import com.whiker.tianchi.o2o.bayes.grade.UserActionGradeGet;
import com.whiker.tianchi.o2o.dao.BatchInsert;
import com.whiker.tianchi.o2o.dao.JdbcRDDRead;
import com.whiker.tianchi.o2o.model.bayes.BayesTrainNormRow;
import org.apache.spark.sql.SparkSession;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author whiker@163.com create on 16-11-6.
 */
public class BayesTrainNormTableBuild {

    public static void main(String[] args) {
        UserActionGradeGet.init();
        MerchantActionGradeGet.init();

        SparkSession sess = SparkSession.builder()
                .appName("BayesTrainNormTableBuild")
                .getOrCreate();

        final String columns = "user_id,merchant_id,coupon_id,discount,distance,action";
        final String insertSql = "INSERT INTO bayes_train_norm VALUES(NULL,?,?,?,?,?,?,?,?)";

        JdbcRDDRead.readAll(sess, "bayes_train_v", columns, BayesTrainNormTableBuild::convert)
                .foreachPartition(iter -> BatchInsert.insert(iter, insertSql, BayesTrainNormTableBuild::insert));
        sess.stop();
    }

    private static BayesTrainNormRow convert(ResultSet r) throws SQLException {
        BayesTrainNormRow row = new BayesTrainNormRow();
        row.setUserId(r.getInt(1));
        row.setMerchantId(r.getInt(2));
        row.setCouponId(r.getInt(3));
        row.setUserGrade(UserActionGradeGet.idToGrade(row.getUserId()));
        row.setMerchantGrade(MerchantActionGradeGet.idToGrade(row.getMerchantId()));
        row.setDiscountGrade(DiscountGradeGet.toGrade(r.getInt(4)));
        row.setDistanceGrade(DistanceGradeGet.toGrade(r.getInt(5)));
        row.setAction(r.getInt(6));
        return row;
    }

    private static void insert(BayesTrainNormRow row, PreparedStatement ps) throws SQLException {
        ps.setInt(1, row.getUserId());
        ps.setInt(2, row.getMerchantId());
        ps.setInt(3, row.getCouponId());
        ps.setDouble(4, row.getUserGrade());
        ps.setInt(5, row.getMerchantGrade());
        ps.setInt(6, row.getDiscountGrade());
        ps.setInt(7, row.getDistanceGrade());
        ps.setInt(8, row.getAction());
    }
}
