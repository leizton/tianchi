package com.whiker.tianchi.o2o.bayes.statis;

import com.whiker.tianchi.o2o.dao.BatchInsert;
import com.whiker.tianchi.o2o.dao.JdbcRDDRead;
import com.whiker.tianchi.o2o.model.bayes.BayesTrainNormRow;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author whiker@163.com create on 16-11-7.
 */
public class BayesStatis {

    private static final String columns = "user_id, merchant_id, coupon_id," +
            "user_grade, merchant_grade, discount_grade, distance_grade, action";

    public static void main(String[] args) {
        SparkSession sess = SparkSession.builder()
                .appName("BayesStatis")
                .getOrCreate();

        JavaRDD<BayesTrainNormRow> rows = JdbcRDDRead.readAll(sess, "bayes_train_norm", columns, BayesStatis::read);
        rows.persist(StorageLevel.MEMORY_ONLY());

        statisNumByAction(rows);

        for (GradeVariable var : GradeVariable.values()) {
            statisNoCond(rows, var);
            statisHasCond(rows, var);
        }
    }

    private static BayesTrainNormRow read(ResultSet r) throws SQLException {
        BayesTrainNormRow row = new BayesTrainNormRow();
        row.setUserId(r.getInt(1));
        row.setMerchantId(r.getInt(2));
        row.setCouponId(r.getInt(3));
        row.setUserGrade(r.getInt(4));
        row.setMerchantGrade(r.getInt(5));
        row.setDiscountGrade(r.getInt(6));
        row.setDistanceGrade(r.getInt(7));
        row.setAction(r.getInt(8));
        return row;
    }

    /**
     * 统计n(C), 用于计算p(C)=n(C)/N
     * N = select count(*) from bayes_train_norm;
     */
    private static void statisNumByAction(JavaRDD<BayesTrainNormRow> rows) {
        final String insertSql = "insert into bayes_statis values(NULL, 2, ?, 0, 0, 0, ?)";
        rows.mapToPair(row -> new Tuple2<>(row.getAction(), 1))
                .reduceByKey((count1, count2) -> count1 + count2)
                .foreachPartition(
                        iter -> BatchInsert.insert(iter, insertSql, (row, ps) -> {
                            ps.setInt(1, row._1);
                            ps.setInt(2, row._2);
                        })
                );
    }

    /**
     * 统计n(X), 用于计算p(X)=n(X)/N
     */
    private static void statisNoCond(JavaRDD<BayesTrainNormRow> rows,
                                     GradeVariable gradeVariable) {
        ConverterNoCond converter = new ConverterNoCond(gradeVariable);
        InserterNoCond inserter = new InserterNoCond(gradeVariable);
        statis(rows, converter, InserterNoCond.insertSql, inserter);
    }

    /**
     * 统计n(X,C), 用于计算p(X|C)=n(X,C)/n(C)
     * p(X,C)=p(C)p(X|C) ==> p(X|C)=p(X,C)/p(C)
     */
    private static void statisHasCond(JavaRDD<BayesTrainNormRow> rows,
                                      GradeVariable gradeVariable) {
        ConverterHasCond converter = new ConverterHasCond(gradeVariable);
        InserterHasCond inserter = new InserterHasCond(gradeVariable);
        statis(rows, converter, InserterHasCond.insertSql, inserter);
    }

    private static <Key> void statis(JavaRDD<BayesTrainNormRow> rows,
                                     PairFunction<BayesTrainNormRow, Key, Integer> convert,
                                     String insertSql,
                                     BatchInsert.Insert<Tuple2<Key, Integer>> insert) {
        rows.mapToPair(convert)
                .reduceByKey((count1, count2) -> count1 + count2)
                .foreachPartition(iter -> BatchInsert.insert(iter, insertSql, insert));
    }

    private static class ConverterNoCond implements PairFunction<BayesTrainNormRow, Integer, Integer>, Serializable {
        private GradeVariable var;

        ConverterNoCond(GradeVariable var) {
            this.var = var;
        }

        @Override
        public Tuple2<Integer, Integer> call(BayesTrainNormRow row) throws Exception {
            return new Tuple2<>(var.getGetGrade().apply(row), 1);
        }
    }

    private static class InserterNoCond implements BatchInsert.Insert<Tuple2<Integer, Integer>>, Serializable {
        private GradeVariable var;
        private static transient final String insertSql = "insert into bayes_statis values(NULL, 0, 0, ?, ?, ?, 0)";

        InserterNoCond(GradeVariable var) {
            this.var = var;
        }

        @Override
        public void insert(Tuple2<Integer, Integer> row, PreparedStatement ps) throws SQLException {
            ps.setInt(1, var.ordinal());
            ps.setInt(2, row._1);
            ps.setInt(3, row._2);
        }
    }

    private static class ConverterHasCond implements PairFunction<BayesTrainNormRow, ConditionKey, Integer>, Serializable {
        private GradeVariable var;

        ConverterHasCond(GradeVariable var) {
            this.var = var;
        }

        @Override
        public Tuple2<ConditionKey, Integer> call(BayesTrainNormRow row) throws Exception {
            ConditionKey key = new ConditionKey();
            key.setAction(row.getAction());
            key.setGrade(var.getGetGrade().apply(row));
            return new Tuple2<>(key, 1);
        }
    }

    private static class InserterHasCond implements BatchInsert.Insert<Tuple2<ConditionKey, Integer>>, Serializable {
        private GradeVariable var;
        private static transient final String insertSql = "insert into bayes_statis values(NULL, 1, ?, ?, ?, ?, 0)";

        InserterHasCond(GradeVariable var) {
            this.var = var;
        }

        @Override
        public void insert(Tuple2<ConditionKey, Integer> row, PreparedStatement ps) throws SQLException {
            ps.setInt(1, row._1.getAction());
            ps.setInt(2, var.ordinal());
            ps.setInt(3, row._1.getGrade());
            ps.setInt(4, row._2);
        }
    }
}
