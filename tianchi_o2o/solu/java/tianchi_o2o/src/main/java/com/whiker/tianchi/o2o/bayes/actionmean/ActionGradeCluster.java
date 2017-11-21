package com.whiker.tianchi.o2o.bayes.actionmean;

import com.whiker.tianchi.o2o.algorithm.Kmeans;
import com.whiker.tianchi.o2o.dao.JdbcRDDRead;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;

/**
 * 用kmeans对用户和商户的action_mean聚类，聚类结果是grade
 *
 * @author whiker@163.com create on 16-11-5.
 */
public class ActionGradeCluster {

    private static final String userGradeQuery =
            "select action_mean,count(user_id) from user_action_mean " +
                    "where ?<=user_id and user_id<=? group by action_mean";

    private static final String merchantGradeQuery =
            "select action_mean,count(merchant_id) from merchant_action_mean " +
                    "where ?<=merchant_id and merchant_id<=? group by action_mean";

    private static long[] CLUSTER_INIT = new long[]{-200, -100, 0, 100, 200, 400};

    public static void main(String[] args) {
        SparkSession sess = SparkSession.builder()
                .appName("ActionGradeCluster")
                .getOrCreate();

        JavaRDD<Kmeans.TrainRow> rows = JdbcRDDRead.readAll(
                sess, merchantGradeQuery, r -> new Kmeans.TrainRow(r.getInt(1), r.getInt(2)));

        Kmeans kmeans = new Kmeans(100, 0, CLUSTER_INIT);
        kmeans.run(rows);
    }
}
