package com.whiker.tianchi.o2o.dao.bayes;

import com.whiker.tianchi.o2o.dao.ConnectManager;
import com.whiker.tianchi.o2o.model.bayes.BayesTrainRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author whiker@163.com create on 16-11-1.
 */
public class BayesTrainRowSave {
    private static final Logger LOGGER = LoggerFactory.getLogger(BayesTrainRowSave.class);

    private static final ThreadLocal<BayesTrainRowDao> threadLocalDaos =
            new ThreadLocal<BayesTrainRowDao>() {
                @Override
                protected BayesTrainRowDao initialValue() {
                    try {
                        BayesTrainRowDao dao = new BayesTrainRowDao();
                        dao.initConnection();
                        return dao;
                    } catch (SQLException e) {
                        LOGGER.error("BayesTrainRowSave init dao error");
                        return null;
                    }
                }
            };

    public static BayesTrainRowDao getDao() {
        return threadLocalDaos.get();
    }

    public static class BayesTrainRowDao {

        private static final int SAVE_BATCH_SIZE = 5000;

        private static final String SAVE_SQL = "INSERT INTO bayes_train VALUES(NULL,?,?,?,?,?,?,?,?)";

        private Connection conn;
        private PreparedStatement ps;
        private int count, totalCount;
        private AtomicBoolean isDestoryed = new AtomicBoolean(false);

        private void initConnection() throws SQLException {
            conn = ConnectManager.getConnection();
            conn.setAutoCommit(false);
        }

        public void add(BayesTrainRow row) {
            try {
                if (isDestoryed.get()) {
                    throw new RuntimeException("dao has destoryed");
                }
                if (ps == null) {
                    ps = conn.prepareStatement(SAVE_SQL);
                }
                ps.setInt(1, row.getUserId());
                ps.setInt(2, row.getMerchantId());
                ps.setInt(3, row.getCouponId());
                ps.setDouble(4, row.getDiscount());
                ps.setInt(5, row.getDistance());
                ps.setInt(6, row.getAction());
                ps.setInt(7, row.getCouponGetDate());
                ps.setInt(8, row.getConsumeDate());
                ps.addBatch();
                if (++count == SAVE_BATCH_SIZE) {
                    save();
                }
            } catch (SQLException e) {
                LOGGER.error("BayesTrainRowDao add error, row:{}", row, e);
            }
        }

        private void save() {
            try {
                ps.executeBatch();
                conn.commit();
                ps = null;
                // LOGGER.info("BayesTrainRowDao save {}", count);
                totalCount += count;
                count = 0;
            } catch (SQLException e) {
                LOGGER.error("BayesTrainRowDao save error", e);
            }
        }

        public int destory() {
            if (this != threadLocalDaos.get()) {
                // 不能destory其他线程的dao
                return totalCount;
            }
            if (isDestoryed.compareAndSet(false, true)) {
                if (count > 0) {
                    save();
                }
                try {
                    conn.close();
                    conn = null;
                } catch (SQLException e) {
                    LOGGER.error("BayesTrainRowDao clean error", e);
                    throw new RuntimeException(e);
                }
                threadLocalDaos.set(null);
            }
            return totalCount;
        }
    }
}
