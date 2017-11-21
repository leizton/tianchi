package com.whiker.tianchi.o2o.dao;

import com.whiker.tianchi.o2o.conf.MysqlConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author whiker@163.com create on 16-10-30.
 */
public class ConnectManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectManager.class);

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private static final ThreadLocal<Connection> threadLocalConns =
            new ThreadLocal<Connection>() {
                @Override
                protected Connection initialValue() {
                    try {
                        return DriverManager.getConnection(MysqlConf.getJdbcUrl());
                    } catch (Exception e) {
                        LOGGER.error("", e);
                        throw new RuntimeException(e);
                    }
                }
            };

    public static Connection getConnection() {
        try {
            Connection conn = threadLocalConns.get();
            if (conn.isClosed()) {
                synchronized (ConnectManager.class) {
                    conn = threadLocalConns.get();
                    if (conn.isClosed()) {
                        conn = openNewConnection();
                        threadLocalConns.set(conn);
                    }
                }
            }
            return conn;
        } catch (SQLException e) {
            LOGGER.error("getConnection error", e);
            throw new RuntimeException(e);
        }
    }

    public static Connection openNewConnection() {
        try {
            return DriverManager.getConnection(MysqlConf.getJdbcUrl());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
