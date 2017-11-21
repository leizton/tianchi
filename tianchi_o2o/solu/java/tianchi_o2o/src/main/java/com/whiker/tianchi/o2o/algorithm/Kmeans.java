package com.whiker.tianchi.o2o.algorithm;

import com.google.common.base.Preconditions;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.storage.StorageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.Serializable;

/**
 * @author whiker@163.com create on 16-11-5.
 */
public class Kmeans implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Kmeans.class);

    /**
     * 仅支持一维整数坐标
     */
    public static class TrainRow implements Serializable {
        private long site;  // 坐标
        private int num;    // 取这个坐标的点数

        public TrainRow(long site, int num) {
            this.site = site;
            this.num = num;
        }
    }

    /**
     * combineByKey的返回类型
     * 新的类中心是siteSum/num
     */
    private static class MergeResult implements Serializable {
        long siteSum;
        int totalNum;

        private MergeResult(long siteSum, int num) {
            this.siteSum = siteSum;
            this.totalNum = num;
        }

        private static MergeResult of(TrainRow row) {
            return new MergeResult(row.site * row.num, row.num);
        }

        private static MergeResult mergeRow(MergeResult ret, TrainRow row) {
            ret.siteSum += row.site * row.num;
            ret.totalNum += row.num;
            return ret;
        }

        private static MergeResult mergeResult(MergeResult ret1, MergeResult ret2) {
            ret1.siteSum += ret2.siteSum;
            ret1.totalNum += ret2.totalNum;
            return ret1;
        }
    }

    // 初始化参数
    private int maxIterNum = Integer.MAX_VALUE;  // 最大迭代次数
    private long maxOffsetThresh = 0;  // 停止迭代时，类中心偏移最大值的阈值
    private long[] centerSites;  // 初始类中心的坐标

    private long[] offsetOfLastIter;

    public Kmeans(int maxIterNum, long maxOffsetThresh, long[] centerSitesInit) {
        Preconditions.checkArgument(maxIterNum >= 0);
        Preconditions.checkArgument(maxOffsetThresh >= 0);
        Preconditions.checkArgument(centerSitesInit != null && centerSitesInit.length > 0);
        this.maxIterNum = maxIterNum;
        this.maxOffsetThresh = maxOffsetThresh;
        this.centerSites = centerSitesInit;
        offsetOfLastIter = new long[centerSitesInit.length];
    }

    /**
     * 运行算法
     */
    public long[] run(JavaRDD<TrainRow> rows) {
        rows.persist(StorageLevel.MEMORY_ONLY());
        print(0);
        for (int iterI = 0; iterI < maxIterNum; ) {
            doIter(rows);
            print(++iterI);
            if (isBelowOfMaxOffsetThresh()) {
                break;
            }
        }
        return centerSites;
    }

    /**
     * 一次迭代
     */
    private void doIter(JavaRDD<TrainRow> rows) {
        rows.mapToPair(this::rowToTuple)
                .combineByKey(MergeResult::of, MergeResult::mergeRow, MergeResult::mergeResult)
                .collectAsMap()
                .entrySet()
                .forEach(e -> computeNewCenter(e.getKey(), e.getValue()));
    }

    /**
     * @return [key:类中心索引, value:trainRow]
     */
    private Tuple2<Integer, TrainRow> rowToTuple(TrainRow row) {
        int belongTo = 0;
        long minDistance = Math.abs(row.site - centerSites[0]);
        for (int i = 1; i < centerSites.length; i++) {
            long distance = Math.abs(row.site - centerSites[i]);
            if (distance < minDistance) {
                minDistance = distance;
                belongTo = i;
            }
        }
        return new Tuple2<>(belongTo, row);
    }

    /**
     * 计算新的类中心
     */
    private void computeNewCenter(int centerId, MergeResult ret) {
        if (ret.totalNum > 0) {
            long newSite = ret.siteSum / ret.totalNum;
            offsetOfLastIter[centerId] = Math.abs(newSite - centerSites[centerId]);
            centerSites[centerId] = newSite;
        }
    }

    /**
     * 打印一次迭代结果
     */
    private void print(int iterI) {
        LOGGER.info("kmeans_[" + iterI + "]_{}_{}", offsetOfLastIter, centerSites);
    }

    /**
     * 检查偏移是否都小于等于阈值
     */
    private boolean isBelowOfMaxOffsetThresh() {
        for (int i = offsetOfLastIter.length - 1; i >= 0; i--) {
            if (offsetOfLastIter[i] > maxOffsetThresh) {
                return false;
            }
        }
        return true;
    }
}
