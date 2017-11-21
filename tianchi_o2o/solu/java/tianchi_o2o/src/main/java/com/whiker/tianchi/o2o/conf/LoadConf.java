package com.whiker.tianchi.o2o.conf;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * @author whiker@163.com create on 16-10-29.
 */
public class LoadConf {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadConf.class);

    /**
     * 读取配置
     */
    public static void load(String file, Consumer<Properties> readConf) {
        ClassLoader classLoader = LoadConf.class.getClassLoader();
        InputStream in = classLoader.getResourceAsStream(file);

        try (AutoCloseInputStream input = new AutoCloseInputStream(in)) {
            Properties conf = new Properties();
            conf.load(input);
            readConf.accept(conf);
        } catch (Exception e) {
            LOGGER.error("load conf error, file:{}", file, e);
            throw new RuntimeException(e);
        }
    }
}
