package com.whiker.tianchi.o2o.bayes.statis;

import scala.Serializable;

/**
 * @author whiker@163.com create on 16-11-8.
 */
public class ConditionKey implements Serializable {
    private int action;
    private int grade;

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof ConditionKey)) {
            return false;
        }
        ConditionKey k = (ConditionKey) o;
        return k.action == action && k.grade == grade;
    }

    @Override
    public int hashCode() {
        return (action << 10) + grade;
    }
}
