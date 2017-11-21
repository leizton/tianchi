package com.whiker.tianchi.o2o.bayes.statis;

import com.whiker.tianchi.o2o.model.bayes.BayesTrainNormRow;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author whiker@163.com create on 16-11-8.
 */
public enum GradeVariable implements Serializable {

    Discount(BayesTrainNormRow::getDiscountGrade),
    Distance(BayesTrainNormRow::getDistanceGrade),
    User(BayesTrainNormRow::getUserGrade),
    Merchant(BayesTrainNormRow::getMerchantGrade);

    private Function<BayesTrainNormRow, Integer> getGrade;

    GradeVariable(Function<BayesTrainNormRow, Integer> getGrade) {
        this.getGrade = getGrade;
    }

    public Function<BayesTrainNormRow, Integer> getGetGrade() {
        return getGrade;
    }
}
