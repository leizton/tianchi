package com.whiker.tianchi.o2o.bayes.probability;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Maps;
import com.whiker.tianchi.o2o.bayes.statis.GradeVariable;
import com.whiker.tianchi.o2o.dao.ExecuteQuerySql;
import com.whiker.tianchi.o2o.model.bayes.BayesStatisRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author whiker@163.com create on 16-11-8.
 */
public class BayesProbability {
    private static final Logger LOGGER = LoggerFactory.getLogger(BayesProbability.class);

    private static int globalNum;

    private static Map<Integer, BayesStatisRow> actionToRowMap;
    private static Map<GradeVariable, Map<Integer, BayesStatisRow>> varValueToRowMap;
    private static Map<GradeVariable, Map<Integer, List<BayesStatisRow>>> varValueToCondRowsMap;

    public static void init() {
        selectCount("bayes_train_norm");
        List<BayesStatisRow> statisRows = loadStatisRows();
        parseStatisRows(statisRows);
    }

    private static void selectCount(String tableName) {
        final String sql = "select count(*) from " + tableName;
        ExecuteQuerySql.query(sql, ret -> {
            if (ret.next()) {
                globalNum = ret.getInt(1);
                LOGGER.info("globalNum: {}", globalNum);
            } else {
                throw new RuntimeException("selectCount " + tableName + " error");
            }
        });
    }

    private static List<BayesStatisRow> loadStatisRows() {
        final String sql = "select flag, action, var_code, var_value, var_num, total_num from bayes_statis";
        List<BayesStatisRow> rows = Lists.newArrayList();
        ExecuteQuerySql.query(sql, ret -> {
            while (ret.next()) {
                BayesStatisRow row = new BayesStatisRow();
                row.flag = ret.getInt(1);
                row.action = ret.getInt(2);
                row.varCode = ret.getInt(3);
                row.varValue = ret.getInt(4);
                row.varNum = ret.getInt(5);
                row.totalNum = ret.getInt(6);
                rows.add(row);
            }
        });
        return rows;
    }

    /**
     * 切分statisRows
     */
    private static void parseStatisRows(List<BayesStatisRow> rows) {
        actionToRowMap = Maps.newHashMap();
        varValueToRowMap = Maps.newHashMap();
        varValueToCondRowsMap = Maps.newHashMap();

        for (BayesStatisRow row : rows) {
            if (row.flag == 2) {
                actionToRowMap.put(row.action, row);
            } else if (row.flag == 0) {
                GradeVariable var = GradeVariable.values()[row.varCode];
                Map<Integer, BayesStatisRow> valueToRow = varValueToRowMap.get(var);
                if (valueToRow == null) {
                    valueToRow = Maps.newHashMap();
                    varValueToRowMap.put(var, valueToRow);
                }
                valueToRow.put(row.varValue, row);
            } else if (row.flag == 1) {
                GradeVariable var = GradeVariable.values()[row.varCode];
                Map<Integer, List<BayesStatisRow>> valueToCondRows = varValueToCondRowsMap.get(var);
                if (valueToCondRows == null) {
                    valueToCondRows = Maps.newHashMap();
                    varValueToCondRowsMap.put(var, valueToCondRows);
                }
                List<BayesStatisRow> condRows = valueToCondRows.get(row.varValue);
                if (condRows == null) {
                    condRows = Lists.newArrayList();
                    valueToCondRows.put(row.varValue, condRows);
                }
                condRows.add(row);
            }
        }
    }

    /**
     * 计算属于action的概率
     */
    public static double compute(int action, int discountGrade, int distanceGrade, int userGrade, int merchantGrade) {
        double actionNum = getActionNum(action);
        final int size = GradeVariable.values().length;
        final int selectSize = 4;

        double[] varCondProbs = new double[]{1, 1, 1, 1};
        varCondProbs[0] = getVarCondNum(GradeVariable.Discount, discountGrade, action);
        varCondProbs[1] = getVarCondNum(GradeVariable.Distance, distanceGrade, action);
        varCondProbs[2] = getVarCondNum(GradeVariable.User, userGrade, action);
        varCondProbs[3] = getVarCondNum(GradeVariable.Merchant, merchantGrade, action);
        for (int i = 0; i < size; i++) {
            if (varCondProbs[i] < 1) {
                return 0;
            }
        }

        double[] varProbs = new double[]{1, 1, 1, 1};
        varProbs[0] = getVarNum(GradeVariable.Discount, discountGrade);
        varProbs[1] = getVarNum(GradeVariable.Distance, distanceGrade);
        varProbs[2] = getVarNum(GradeVariable.User, userGrade);
        varProbs[3] = getVarNum(GradeVariable.Merchant, merchantGrade);

        double A = 0, B = 0;
        for (int i = 0; i < size; i++) {
            A += Math.log(varCondProbs[i]);
        }
        for (int i = 0; i < size; i++) {
            B += Math.log(varProbs[i]);
        }
        A += (selectSize - 1) * Math.log(globalNum);
        B += (selectSize - 1) * Math.log(actionNum);
        return Math.exp(A) / Math.exp(B);
    }

    private static int getActionNum(int action) {
        BayesStatisRow row = actionToRowMap.get(action);
        return row == null ? 0 : row.totalNum;
    }

    private static int getVarCondNum(GradeVariable var, int varValue, int action) {
        Map<Integer, List<BayesStatisRow>> valueToCondRows = varValueToCondRowsMap.get(var);
        if (valueToCondRows == null) {
            return 0;
        }
        List<BayesStatisRow> condRows = valueToCondRows.get(varValue);
        if (condRows == null) {
            return 0;
        }
        for (BayesStatisRow row : condRows) {
            if (row.action == action) {
                return row.varNum;
            }
        }
        return 0;
    }

    private static int getVarNum(GradeVariable var, int varValue) {
        Map<Integer, BayesStatisRow> valueToRow = varValueToRowMap.get(var);
        if (valueToRow == null) {
            return 0;
        }
        BayesStatisRow row = valueToRow.get(varValue);
        return row == null ? 0 : row.varNum;
    }
}
