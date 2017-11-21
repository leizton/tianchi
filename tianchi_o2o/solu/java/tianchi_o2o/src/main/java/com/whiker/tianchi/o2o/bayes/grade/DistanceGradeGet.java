package com.whiker.tianchi.o2o.bayes.grade;

/**
 * @author whiker@163.com create on 16-11-6.
 */
public class DistanceGradeGet {
    public static int toGrade(int distance) {
        return distance == 0 || distance == 1 ? 0 : 1;
    }
}
