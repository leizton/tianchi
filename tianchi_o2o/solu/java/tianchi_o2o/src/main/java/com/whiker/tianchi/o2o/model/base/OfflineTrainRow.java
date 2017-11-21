package com.whiker.tianchi.o2o.model.base;

/**
 * 线下训练数据的一行记录
 *
 * @author whiker@163.com create on 16-10-29.
 */
public class OfflineTrainRow {
    private int userId;
    private int merchantId;
    private int couponId;
    private double couponRate;
    private int distance;
    private int couponGetDate;
    private int consumeDate;

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

    public double getCouponRate() {
        return couponRate;
    }

    public void setCouponRate(double couponRate) {
        this.couponRate = couponRate;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getCouponGetDate() {
        return couponGetDate;
    }

    public void setCouponGetDate(int couponGetDate) {
        this.couponGetDate = couponGetDate;
    }

    public int getConsumeDate() {
        return consumeDate;
    }

    public void setConsumeDate(int consumeDate) {
        this.consumeDate = consumeDate;
    }
}
