package com.whiker.tianchi.o2o.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * @author whiker@163.com create on 16-11-4.
 */
public class BatchInsert {

    private static final int BATCH_SIZE = 5000;

    public interface Insert<T> {
        void insert(T row, PreparedStatement ps) throws SQLException;
    }

    public static <T> void insert(Iterator<T> input, String insertSql, Insert<T> output) {
        Connection conn = ConnectManager.openNewConnection();
        insert(conn, input, insertSql, output);
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void insert(Connection conn, Iterator<T> input, String insertSql, Insert<T> output) {
        try {
            conn.setAutoCommit(false);
            while (input.hasNext()) {
                PreparedStatement ps = conn.prepareStatement(insertSql);
                for (int i = 0; i < BATCH_SIZE && input.hasNext(); i++) {
                    output.insert(input.next(), ps);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
