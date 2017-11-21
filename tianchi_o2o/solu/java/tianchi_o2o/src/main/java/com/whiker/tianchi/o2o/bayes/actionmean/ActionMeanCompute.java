package com.whiker.tianchi.o2o.bayes.actionmean;

import com.whiker.tianchi.o2o.dao.BatchInsert;
import com.whiker.tianchi.o2o.dao.JdbcRDDRead;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 计算每个用户的action_mean
 *
 * @author whiker@163.com create on 16-11-3.
 */
public class ActionMeanCompute {

    public static void main(String[] args) {
        SparkSession sess = SparkSession.builder()
                .appName("ActionMeanCompute")
                .getOrCreate();

        computeActionMean(sess, "bayes_train_v", "user_id,action", "user_action_mean");
        computeActionMean(sess, "bayes_train_v", "merchant_id,action", "merchant_action_mean");
    }

    private static void computeActionMean(SparkSession sess, String inputTableName, String inputColumns, String outputTableName) {
        final String outputSql = "insert into " + outputTableName + " values(?, ?)";
        JdbcRDDRead.readAll(sess, inputTableName, inputColumns, ActionMeanCompute::readSrcData)
                .filter(e -> e != null)
                .mapToPair(tuple -> tuple)
                .reduceByKey((v1, v2) -> new Value(v1.actionSum + v2.actionSum, v1.count + v2.count))
                .foreachPartition(iter -> BatchInsert.insert(iter, outputSql, ActionMeanCompute::insert));
    }

    private static Tuple2<Integer, Value> readSrcData(ResultSet r) throws SQLException {
        int id = r.getInt(1);
        int action = r.getInt(2);
        return action == 0 ? null : new Tuple2<>(id, new Value(action, 1));
    }

    private static void insert(Tuple2<Integer, Value> row, PreparedStatement ps) throws SQLException {
        ps.setInt(1, row._1);
        ps.setInt(2, 100 * row._2.actionSum / row._2.count);
    }

    private static class Value implements Serializable {
        int actionSum;
        int count;

        private Value(int actionSum, int count) {
            this.actionSum = actionSum;
            this.count = count;
        }
    }
}
