package com.whiker.tianchi.o2o.model.bayes;

/**
 * @author whiker@163.com create on 16-10-30.
 */
public class BayesTrainRow {
    private int userId;
    private int merchantId;
    private int couponId;
    private int discount;
    private int distance;
    private int action;
    private int couponGetDate;
    private int consumeDate;

    public BayesTrainRow(int userId, int merchantId, int couponId,
                         int discount, int distance, int action,
                         int couponGetDate, int consumeDate) {
        this.userId = userId;
        this.merchantId = merchantId;
        this.couponId = couponId;
        this.discount = discount;
        this.distance = distance;
        this.action = action;
        this.couponGetDate = couponGetDate;
        this.consumeDate = consumeDate;
    }

    public int getUserId() {
        return userId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public int getCouponId() {
        return couponId;
    }

    public int getDiscount() {
        return discount;
    }

    public int getDistance() {
        return distance;
    }

    public int getAction() {
        return action;
    }

    public int getCouponGetDate() {
        return couponGetDate;
    }

    public int getConsumeDate() {
        return consumeDate;
    }

    @Override
    public String toString() {
        return "[bayesTrainRow:"
                + userId + ","
                + merchantId + ","
                + couponId + ","
                + discount + ","
                + distance + ","
                + action + ","
                + couponGetDate + ","
                + consumeDate + "]";
    }
}
