package com.whiker.tianchi.o2o.model.bayes;

import java.io.Serializable;

/**
 * @author yiqun.fan create on 16-11-8.
 */
public class BayesForecastRow implements Serializable {
    public int id;
    public int userId;
    public int merchantId;

    public int couponId;
    public int discount;
    public int distance;
    public int couponGetDate;

    public int userGrade;
    public int merchantGrade;
    public int discountGrade;
    public int distanceGrade;

    public double probablity;
}
