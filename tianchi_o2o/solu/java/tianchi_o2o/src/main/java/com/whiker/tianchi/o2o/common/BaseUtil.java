package com.whiker.tianchi.o2o.common;

import com.clearspring.analytics.util.Lists;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import java.util.List;
import java.util.function.Function;

/**
 * @author whiker@163.com create on 16-11-6.
 */
public class BaseUtil {
    /**
     * 字符串转list
     *
     * @param s       例如: "[1, 2, 3]"
     * @param split   分隔符
     * @param convert 字符串转T的函数
     */
    public static <T> List<T> stringToList(String s, String split, Function<String, T> convert) {
        Preconditions.checkArgument(s != null && s.length() >= 2, "字符串长度至少是2, " + s);
        Preconditions.checkArgument(s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']', "应该以[]包裹数据, " + s);
        s = s.substring(1, s.length() - 1);

        List<T> ret = Lists.newArrayList();
        for (String s1 : Splitter.on(split).trimResults().split(s)) {
            ret.add(convert.apply(s1));
        }
        return ret;
    }
}
