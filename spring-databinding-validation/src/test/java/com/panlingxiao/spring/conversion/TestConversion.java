package com.panlingxiao.spring.conversion;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by panlingxiao on 2016/5/30.
 */
public class TestConversion {

    @Test
    public void testConversionFactory(){

    }


    @Test
    public void testDefaultConversionService(){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("conversion-test.xml");

    }
}
