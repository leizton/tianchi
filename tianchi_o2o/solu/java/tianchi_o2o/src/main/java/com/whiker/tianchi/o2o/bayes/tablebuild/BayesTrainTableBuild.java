package com.whiker.tianchi.o2o.bayes.tablebuild;

import com.clearspring.analytics.util.Lists;
import com.whiker.tianchi.o2o.dao.JdbcRDDRead;
import com.whiker.tianchi.o2o.dao.bayes.BayesTrainRowSave;
import com.whiker.tianchi.o2o.model.bayes.BayesTrainRow;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 构造表bayes_train
 *
 * @author whiker@163.com create on 16-10-30.
 */
public class BayesTrainTableBuild {
    private static final Logger LOGGER = LoggerFactory.getLogger(BayesTrainTableBuild.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        SparkSession sess = SparkSession.builder()
                .appName("BayesTrainTableBuild")
                .getOrCreate();

        final String columns = "user_id,merchant_id,coupon_id,coupon_rate,distance,coupon_get_date,consume_date";
        JdbcRDDRead.readAll(sess, "offline_train", columns, BayesTrainTableBuild::couponMerge)
                .mapToPair(tuple -> tuple)
                .groupByKey()
                .foreachPartition(it -> {
                    BayesTrainRowSave.BayesTrainRowDao dao = BayesTrainRowSave.getDao();
                    while (it.hasNext()) {
                        analyseActionAndSave(it.next(), dao);
                    }
                    LOGGER.info("save {}", dao.destory());
                });
    }

    /**
     * 优惠券和优惠率合并成变量discount
     * key: [user_id, merchant_id]
     * value: [coupon_id, discount, distance, coupon_get_date, consume_data]
     */
    private static Tuple2<Key, Value> couponMerge(ResultSet r) {
        try {
            Key k = new Key(r.getInt(1), r.getInt(2));
            int couponId = r.getInt(3);
            int discount = 0;
            if (couponId >= 0) {
                int couponRate = (int) (r.getDouble(4) * 100);
                couponRate = Math.max(0, Math.min(100, couponRate));
                discount = 20 - couponRate / 5;
            }
            Value v = new Value(couponId, discount, r.getInt(5), r.getInt(6), r.getInt(7));
            return new Tuple2<>(k, v);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * 分析出用户的action
     */
    private static void analyseActionAndSave(Tuple2<Key, Iterable<Value>> input,
                                             BayesTrainRowSave.BayesTrainRowDao dao) {
        Key key = input._1();
        List<Value> leaveValues = analyseUseCouponToConsume(key, input._2(), dao);
        analyseNoUseCouponToConsume(key, leaveValues, dao);
    }

    /**
     * 分析行为中用券消费的这一部分
     * 结果的action > 0
     */
    private static List<Value> analyseUseCouponToConsume(Key key, Iterable<Value> allValues,
                                                         BayesTrainRowSave.BayesTrainRowDao dao) {
        List<Value> leaveValues = Lists.newArrayList();
        for (Value v : allValues) {
            if (v.couponGetDate >= 0 && v.consumeDate >= 0) {
                // 用券消费
                int action, diffDate = v.consumeDate - v.couponGetDate;
                if (diffDate < 5) {
                    action = 4;
                } else if (diffDate < 15) {
                    action = 2;
                } else {
                    action = 1;
                }
                dao.add(buildRow(key, v, action));
            } else if (v.couponGetDate >= 0 || v.consumeDate >= 0) {
                // 剩余的value存入leave列表中
                leaveValues.add(v);
            }
        }
        return leaveValues;
    }

    /**
     * 分析行为中不是用券消费的部分
     * 结果的action <= 0
     */
    private static void analyseNoUseCouponToConsume(Key key, List<Value> leaveValues,
                                                    BayesTrainRowSave.BayesTrainRowDao dao) {
        // 按dateFlag从小到大排序
        for (Value v : leaveValues) {
            v.dateFlag = v.couponGetDate >= 0 ? v.couponGetDate : v.consumeDate;
        }
        leaveValues.sort((v1, v2) -> v1.dateFlag - v2.dateFlag);

        final int leaveNum = leaveValues.size();

        for (int i = 0; i < leaveNum; i++) {
            Value curr = leaveValues.get(i);
            int action = 0;

            // 分析出action
            if (curr.couponGetDate < 0 && curr.consumeDate >= 0) {
                // 没有优惠券的普通消费
                action = 0;
            } else if (curr.couponGetDate >= 0 && curr.consumeDate < 0) {
                // 有券，但这张券没用于消费
                // 查看后面15天内是否有消费
                boolean isHaveConsumeIn15Days = false;
                for (int j = i + 1; j < leaveNum; j++) {
                    Value v1 = leaveValues.get(j);
                    if (v1.dateFlag - curr.dateFlag >= 15) {
                        // 超过15天, i后面15天内没有消费
                        break;
                    }
                    if (v1.consumeDate >= 0) {
                        // i后面15天内有消费, 即消费时有券不用
                        isHaveConsumeIn15Days = true;
                        break;
                    }
                }
                action = isHaveConsumeIn15Days ? -2 : -1;
            }

            dao.add(buildRow(key, curr, action));
        }
    }

    /**
     * 由key/value/action构造BayesTrainRow
     */
    private static BayesTrainRow buildRow(Key k, Value v, int action) {
        return new BayesTrainRow(
                k.userId, k.merchantId, v.couponId,
                v.discount, v.distance, action,
                v.couponGetDate, v.consumeDate);
    }

    /**
     * key
     */
    private static class Key implements Serializable {
        int userId;
        int merchantId;

        Key(int userId, int merchantId) {
            this.userId = userId;
            this.merchantId = merchantId;
        }

        @Override
        public boolean equals(Object k) {
            if (k == null) {
                return false;
            }
            if (k == this) {
                return true;
            }
            if (!(k instanceof Key)) {
                return false;
            }
            Key key = (Key) k;
            return key.userId == userId && key.merchantId == merchantId;
        }

        @Override
        public int hashCode() {
            return (userId << 8) + merchantId;
        }
    }

    /**
     * value
     */
    private static class Value implements Serializable {
        int couponId;
        int discount;
        int distance;
        int couponGetDate;
        int consumeDate;
        int dateFlag;

        Value(int couponId, int discount, int distance, int couponGetDate, int consumeDate) {
            this.couponId = couponId;
            this.discount = discount;
            this.distance = distance;
            this.couponGetDate = couponGetDate;
            this.consumeDate = consumeDate;
        }
    }
}
