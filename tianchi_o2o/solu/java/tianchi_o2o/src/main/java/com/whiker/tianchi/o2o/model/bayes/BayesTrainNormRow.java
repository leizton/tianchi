package com.whiker.tianchi.o2o.model.bayes;

/**
 * @author whiker@163.com create on 16-11-6.
 */
public class BayesTrainNormRow {
    private int userId;
    private int merchantId;
    private int couponId;
    private int userGrade;
    private int merchantGrade;
    private int discountGrade;
    private int distanceGrade;
    private int action;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public int getCouponId() {
        return couponId;
    }

    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }

    public int getUserGrade() {
        return userGrade;
    }

    public void setUserGrade(int userGrade) {
        this.userGrade = userGrade;
    }

    public int getMerchantGrade() {
        return merchantGrade;
    }

    public void setMerchantGrade(int merchantGrade) {
        this.merchantGrade = merchantGrade;
    }

    public int getDiscountGrade() {
        return discountGrade;
    }

    public void setDiscountGrade(int discountGrade) {
        this.discountGrade = discountGrade;
    }

    public int getDistanceGrade() {
        return distanceGrade;
    }

    public void setDistanceGrade(int distanceGrade) {
        this.distanceGrade = distanceGrade;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
