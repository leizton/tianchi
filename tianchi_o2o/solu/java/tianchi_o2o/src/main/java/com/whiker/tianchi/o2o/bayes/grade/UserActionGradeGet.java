package com.whiker.tianchi.o2o.bayes.grade;

import java.util.Map;

/**
 * @author whiker@163.com create on 16-11-6.
 */
public class UserActionGradeGet extends ActionGradeGet {

    private static Map<Integer, Integer> idToGradeMap;

    public static void init() {
        idToGradeMap = init("userCenters", "user_action_mean", "user_id");
    }

    public static int idToGrade(int id) {
        Integer grade = idToGradeMap.get(id);
        return grade != null ? grade : 2;
    }
}
