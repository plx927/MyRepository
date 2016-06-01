package com.panlingxiao.spring.formatter;

import org.junit.Test;
import org.springframework.format.datetime.DateFormatter;

import java.text.ParseException;
import java.util.Locale;

/**
 * Created by panlingxiao on 2016/6/1.
 */
public class TestFormatter {

    /**
     * 默认格式化为严格模式,因此无法格式化12:99:22,需要设置为lenient为true才可以。
     * @throws Exception
     */
    @Test(expected = ParseException.class)
    public void testFormatDate() throws Exception{
        DateFormatter formatter = new DateFormatter();
        formatter.setPattern("yyyy-MM-dd hh:mm:ss");
        formatter.parse("2015-12-12 12:99:22", Locale.getDefault());
    }




}
