package com.whiker.tianchi.o2o.bayes.grade;

/**
 * @author whiker@163.com create on 16-11-6.
 */
public class DiscountGradeGet {
    public static int toGrade(int discount) {
		if (discount <= 1) {
            return 0;
        } else if (discount < 5) {
            return 1;
        } else if (discount == 5) {
            return 2;
        } else if (discount < 10) {
            return 3;
        } else {
            return 4;
        }
    }
}
