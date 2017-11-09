package com.panlingxiao.spring.formatter;

import com.panlingxiao.spring.validation.domain.Circle;
import com.panlingxiao.spring.validation.domain.Person;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.datetime.DateFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Created by panlingxiao on 2016/6/1.
 */
public class TestFormatter {

    @Test
    public void testDateFormat(){
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
        String value = format.format(new Date());
        System.out.println(value);
    }

    /**
     * 默认格式化为严格模式,因此无法格式化12:99:22,需要设置为lenient为true才可以。
     *
     * @throws Exception
     */
    @Test(expected = ParseException.class)
    public void testFormatDate() throws Exception {
        DateFormatter formatter = new DateFormatter();
        formatter.setPattern("yyyy-MM-dd hh:mm:ss");
        formatter.parse("2015-12-12 12:99:22", Locale.getDefault());
    }



    @Test
    public void testFormattingService() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("conv-format-app-context.xml");
        Circle circle = ctx.getBean("circle", Circle.class);
        Assert.assertEquals(1, circle.getPoint().getX());
        Assert.assertEquals(2,circle.getPoint().getY());
        ConversionService cs = ctx.getBean(AbstractApplicationContext.CONVERSION_SERVICE_BEAN_NAME, ConversionService.class);
        String result = cs.convert(circle.getPoint(),String.class);
        System.out.println(result);
    }


    @Test
    public void testDateTimeFormatter(){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("conv-format-app-context.xml");
        ctx.getBean("person", Person.class);
    }
}
