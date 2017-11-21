package com.whiker.tianchi.o2o.bayes.grade;

import com.google.common.collect.Maps;
import com.whiker.tianchi.o2o.common.BaseUtil;
import com.whiker.tianchi.o2o.conf.LoadConf;
import com.whiker.tianchi.o2o.dao.ConnectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * @author whiker@163.com create on 16-11-6.
 */
abstract class ActionGradeGet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionGradeGet.class);

    private static final int BATCH_SIZE = 10000;

    private static int[] splitThresh;

    static Map<Integer, Integer> init(String confName, String tableName, String idName) {
        try {
            loadSplitThresh(confName);
            return initIdToGradeMap(tableName, idName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadSplitThresh(String confName) {
        LoadConf.load("action_cluster_center.properties",
                conf -> splitThresh = parseSplitThreshFromString(conf.getProperty(confName)));
        LOGGER.info("loadSplitThresh, {}, {}", confName, splitThresh);
    }

    private static int[] parseSplitThreshFromString(String s) {
        List<Integer> centers = BaseUtil.stringToList(s, ",", Integer::parseInt);
        int[] splitThresh = new int[centers.size() - 1];
        for (int i = 0; i < splitThresh.length; i++) {
            splitThresh[i] = (centers.get(i) + centers.get(i + 1)) / 2;
        }
        return splitThresh;
    }

    private static Map<Integer, Integer> initIdToGradeMap(String tableName, String idName) throws SQLException {
        Map<Integer, Integer> idToGradeMap = Maps.newHashMap();
        Connection conn = ConnectManager.getConnection();
        final String sql = "select " + idName + ",action_mean from " + tableName + " limit ";

        int readNum = BATCH_SIZE;
        for (int offset = 0; readNum >= BATCH_SIZE; offset += BATCH_SIZE) {
            Statement st = conn.createStatement();
            ResultSet ret = st.executeQuery(sql + offset + "," + BATCH_SIZE);
            readNum = 0;
            while (ret.next()) {
                readNum++;
                idToGradeMap.put(ret.getInt(1), actionMeanToGrade(ret.getInt(2)));
            }
            ret.close();
            st.close();
        }

        conn.close();
        LOGGER.info("initIdToGradeMap, {} num: {}", idName, idToGradeMap.size());
        return idToGradeMap;
    }

    private static int actionMeanToGrade(int actionMean) {
        for (int i = 0; i < splitThresh.length; i++) {
            if (actionMean < splitThresh[i]) {
                return i;
            }
        }
        return splitThresh.length;
    }
}
