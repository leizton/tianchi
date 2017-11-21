package com.whiker.tianchi.o2o.bayes.grade;

import java.util.Map;

/**
 * @author whiker@163.com create on 16-11-6.
 */
public class MerchantActionGradeGet extends ActionGradeGet {

    private static Map<Integer, Integer> idToGradeMap;

    public static void init() {
        idToGradeMap = init("merchantCenters", "merchant_action_mean", "merchant_id");
    }

    public static int idToGrade(int id) {
        Integer grade = idToGradeMap.get(id);
        return grade != null ? grade : 2;
    }
}
