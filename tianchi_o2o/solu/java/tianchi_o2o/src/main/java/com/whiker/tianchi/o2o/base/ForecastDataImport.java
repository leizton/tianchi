package com.whiker.tianchi.o2o.base;

import com.whiker.tianchi.o2o.common.Constants;
import com.whiker.tianchi.o2o.common.DateUtil;
import com.whiker.tianchi.o2o.conf.InputConf;
import com.whiker.tianchi.o2o.dao.BatchInsert;
import com.whiker.tianchi.o2o.model.base.ForecastRow;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 导入预测样本数据
 *
 * @author whiker@163.com create on 16-11-6.
 */
public class ForecastDataImport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForecastDataImport.class);

    private static final String INSERT_SQL = "INSERT INTO forecast VALUES(NULL,?,?,?,?,?,?)";

    public static void main(String[] args) {
        SparkSession sess = SparkSession.builder()
                .appName("ForecastDataImport")
                .getOrCreate();

        JavaSparkContext sc = new JavaSparkContext(sess.sparkContext());
        sc.textFile(InputConf.getForecastInputFile())
                .map(ForecastDataImport::convert)
                .filter(row -> row != null)
                .foreachPartition(iter -> BatchInsert.insert(iter, INSERT_SQL, ForecastDataImport::insert));
    }

    private static ForecastRow convert(String input) {
        try {
            String[] data = input.split(Constants.fileLineSeparator);
            ForecastRow row = new ForecastRow();
            row.setUserId(Integer.parseInt(data[0]));
            row.setMerchantId(Integer.parseInt(data[1]));
            row.setCouponId(data[2].equals("null") ? -1 : Integer.parseInt(data[2]));
            row.setCouponRate(row.getCouponId() >= 0 ? CouponRateConvert.convertCouponRate(data[3]) : 0);
            row.setDistance(data[4].equals("null") ? -1 : Integer.parseInt(data[4]));
            row.setCouponGetDate(data[5].equals("null") ? -1 : DateUtil.toDate(data[5]));
            return row;
        } catch (Exception e) {
            LOGGER.error("convert error, input:{}", input, e);
            return null;
        }
    }

    private static void insert(ForecastRow row, PreparedStatement ps) throws SQLException {
        ps.setInt(1, row.getUserId());
        ps.setInt(2, row.getMerchantId());
        ps.setInt(3, row.getCouponId());
        ps.setDouble(4, row.getCouponRate());
        ps.setInt(5, row.getDistance());
        ps.setInt(6, row.getCouponGetDate());
    }
}
