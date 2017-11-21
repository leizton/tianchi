package com.whiker.tianchi.o2o.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author yiqun.fan create on 16-11-9.
 */
public class ExecuteQuerySql {

    public interface Read {
        void read(ResultSet ret) throws SQLException;
    }

    public static void query(String sql, Read read) {
        try {
            Connection conn = ConnectManager.getConnection();
            Statement st = conn.createStatement();
            ResultSet ret = st.executeQuery(sql);
            read.read(ret);
            ret.close();
            st.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
